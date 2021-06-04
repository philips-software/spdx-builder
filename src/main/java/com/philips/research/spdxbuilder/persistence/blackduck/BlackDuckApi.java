/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.License;
import com.philips.research.spdxbuilder.core.domain.LicenseParser;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.http.*;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Black Duck REST API declaration for Retrofit2
 */
public interface BlackDuckApi {
    String USER_4_JSON = "Accept: application/vnd.blackducksoftware.user-4+json";
    String STATUS_4_JSON = "Accept: application/vnd.blackducksoftware.status-4+json";
    String PROJECT_DETAIL_4_JSON = "Accept: application/vnd.blackducksoftware.project-detail-4+json";
    String PROJECT_DETAIL_5_JSON = "Accept: application/vnd.blackducksoftware.project-detail-5+json";
    String BILL_OF_MATERIALS_6_JSON = "Accept: application/vnd.blackducksoftware.bill-of-materials-6+json";
    String COMPONENT_DETAIL_4_JSON = "Accept: application/vnd.blackducksoftware.component-detail-4+json";

    @Headers(USER_4_JSON)
    @POST("/api/tokens/authenticate")
    Call<AuthJson> authenticate(@Header("Authorization") String authHeader);

    @Headers(STATUS_4_JSON)
    @GET("/api/current-version")
    Call<VersionJson> serverVersion();

    @Headers(PROJECT_DETAIL_4_JSON)
    @GET("/api/projects")
    Call<ItemsJson<ProjectJson>> findProjects(@Query("q") String filter);

    @Headers(PROJECT_DETAIL_5_JSON)
    @GET("/api/projects/{projectId}/versions")
    Call<ItemsJson<ProjectVersionJson>> findProjectVersions(@Path("projectId") UUID projectId, @Query("q") String filter);

    @Headers(BILL_OF_MATERIALS_6_JSON)
    @GET("/api/projects/{projectId}/versions/{versionId}/components?limit=9999")
    Call<ItemsJson<ComponentVersionJson>> getBomComponents(@Path("projectId") UUID projectId, @Path("versionId") UUID versionId);

    @Headers(BILL_OF_MATERIALS_6_JSON)
    @GET("/api/projects/{projectId}/versions/{versionId}/hierarchical-components?limit=9999")
    Call<ItemsJson<ComponentVersionJson>> getRootComponentVersions(@Path("projectId") UUID projectId, @Path("versionId") UUID versionId);

    @Headers(BILL_OF_MATERIALS_6_JSON)
    @GET("/api/projects/{projectId}/versions/{versionId}/components/{componentId}/versions/{componentVersionId}/hierarchical-components/{hierarchicalId}/children?limit=999")
    Call<ItemsJson<ComponentVersionJson>> getChildComponentVersions(@Path("projectId") UUID projectId,
                                                                    @Path("versionId") UUID versionId,
                                                                    @Path("componentId") UUID componentId,
                                                                    @Path("componentVersionId") UUID componentVersionId,
                                                                    @Path("hierarchicalId") long hierarchicalId);

    @Headers(COMPONENT_DETAIL_4_JSON)
    @GET("/api/components/{componentId}")
    Call<ComponentJson> getComponent(@Path("componentId") UUID componentId);

    @SuppressWarnings("NotNullFieldNotInitialized")
    class AuthJson {
        String bearerToken;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class VersionJson {
        String version;
    }

    class ItemsJson<T> {
        List<T> items = new ArrayList<>();
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class LinkJson {
        String rel;
        URI href;
    }

    class ProjectJson implements BlackDuckProduct {
        String name = "";
        @NullOr String description;
        @SuppressWarnings("NotNullFieldNotInitialized")
        LinkJson _meta;

        @Override
        public UUID getId() {
            return UriHelper.uuidFromUri(_meta.href, 0);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<License> getLicense() {
            return Optional.empty();
        }
    }

    class ProjectVersionJson implements BlackDuckProduct {
        String versionName = "";
        @NullOr String releaseComments;
        @NullOr LicenseJson license;
        @SuppressWarnings("NotNullFieldNotInitialized")
        LinkJson _meta;

        @Override
        public UUID getId() {
            return UriHelper.uuidFromUri(_meta.href, 0);
        }

        @Override
        public String getName() {
            return versionName;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(releaseComments);
        }

        @Override
        public Optional<License> getLicense() {
            return (license != null) ? Optional.of(license.getLicense()) : Optional.empty();
        }
    }

    class LinksJson {
        @SuppressWarnings("NotNullFieldNotInitialized")
        URI href;
        List<LinkJson> links = new ArrayList<>();
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class ComponentVersionJson implements BlackDuckComponent {
        String componentName;
        String componentVersionName;
        URI componentVersion;
        String componentType;

        List<String> usages = new ArrayList<>();
        List<String> matchTypes = new ArrayList<>();
        List<OriginJson> origins = new ArrayList<>();
        List<LicenseJson> licenses = new ArrayList<>();
        LinksJson _meta;

        @Override
        public UUID getId() {
            return UriHelper.uuidFromUri(componentVersion, 2);
        }

        @Override
        public String getName() {
            return componentName;
        }

        @Override
        public String getVersion() {
            return componentVersionName;
        }

        @Override
        public UUID getVersionId() {
            return UriHelper.uuidFromUri(componentVersion, 0);
        }

        @Override
        public List<PackageURL> getPackageUrls() {
            return origins.stream()
                    .map(PackageIdentifier::getPurl)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> getUsages() {
            return usages;
        }

        @Override
        public License getLicense() {
            return (licenses.size() > 0) ? licenses.get(0).getLicense() : License.NONE;
        }

        @Override
        public long getHierarchicalId() {
            return _meta.links.stream()
                    .filter(link -> "children".equalsIgnoreCase(link.rel))
                    .findAny()
                    .map(link -> UriHelper.longFromUri(link.href, 1))
                    .orElse(0L);
        }

        @Override
        public boolean isSubproject() {
            return "SUB_PROJECT".equals(componentType);
        }

        @Override
        public boolean isAdditionalComponent() {
            return isSubproject() || matchTypes.contains("MANUAL_BOM_COMPONENT");
        }

        @Override
        public String toString() {
            return componentName + ' ' + componentVersionName;
        }
    }

    class OriginJson extends PackageIdentifier {
        OriginJson() {
            //noinspection ConstantConditions
            super(null, null);
        }

        @Override
        Optional<PackageURL> getPurl() {
            if ("unknown".equals(externalNamespace)) {
                return Optional.empty();
            }
            return super.getPurl();
        }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class LicenseJson {
        String licenseDisplay;
        @NullOr String spdxId;
        String licenseType;
        List<LicenseJson> licenses = new ArrayList<>();

        public License getLicense() {
            if (licenses.isEmpty()) {
                final var identifier = (spdxId != null) ? spdxId : licenseDisplay;
                return LicenseParser.parse(identifier);
            }
            final var disjunctive = "DISJUNCTIVE".equals(licenseType);
            return licenses.stream().map(LicenseJson::getLicense)
                    .reduce(License.NONE, (l, r) -> disjunctive ? l.or(r) : l.and(r));
        }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class ComponentJson implements BlackDuckComponentDetails {
        String description;
        @NullOr URL url;

        @Override
        public Optional<String> getDescription() {
            return description.isBlank() ? Optional.empty() : Optional.of(description);
        }

        @Override
        public Optional<URL> getHomepage() {
            return Optional.ofNullable(url);
        }
    }
}

