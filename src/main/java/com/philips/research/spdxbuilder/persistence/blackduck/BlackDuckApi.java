/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import retrofit2.Call;
import retrofit2.http.*;

import java.net.URI;
import java.util.*;

/**
 * Black Duck REST API declaration for Retrofit2
 */
public interface BlackDuckApi {
    @Headers({"Accept: application/vnd.blackducksoftware.user-4+json"})
    @POST("/api/tokens/authenticate")
    Call<AuthJson> authenticate(@Header("Authorization") String authHeader);

    @Headers({"Accept: application/vnd.blackducksoftware.status-4+json"})
    @GET("/api/current-version")
    Call<VersionJson> serverVersion();

    @Headers({"Accept: application/vnd.blackducksoftware.project-detail-4+json"})
    @GET("/api/projects")
    Call<ItemsJson<ProjectJson>> findProjects(@Query("q") String filter);

    @Headers({"Accept: application/vnd.blackducksoftware.project-detail-5+json"})
    @GET("/api/projects/{projectId}/versions")
    Call<ItemsJson<ProjectVersionJson>> findProjectVersions(@Path("projectId") UUID projectId, @Query("q") String filter);

    @Headers({"Accept: application/vnd.blackducksoftware.bill-of-materials-6+json"})
    @GET("/api/projects/{projectId}/versions/{versionId}/components?limit=9999")
    Call<ItemsJson<ComponentJson>> readComponents(@Path("projectId") UUID projectId, @Path("versionId") UUID versionId);

    @Headers({"Accept: application/vnd.blackducksoftware.component-detail-4+json"})
    @GET("/api/components/{componentId}/versions/{componentVersionId}/origins/{originId}/direct-dependencies?limit=999")
    Call<ItemsJson<DependencyJson>> readDependencies(@Path("componentId") UUID componentId,
                                                     @Path("componentVersionId") UUID componentVersionId,
                                                     @Path("originId") UUID originId);

    class AuthJson {
        String bearerToken;
    }

    class VersionJson {
        String version;
    }

    class ItemsJson<T> {
        List<T> items = new ArrayList<>();
    }

    abstract class EntityJson {
        MetaJson _meta;

        UUID getId() {
            final var path = _meta.href.getPath().split("/");
            try {
                return UUID.fromString(path[path.length-1]);
            } catch (IllegalArgumentException e) {
                return UUID.fromString(path[path.length-2]);
            }
        }
    }

    class MetaJson {
        URI href;
    }

    class ProjectJson extends EntityJson {
        String name;
        String description;
    }

    class ProjectVersionJson extends EntityJson {
        String versionName;
        String phase;
        String distribution;
    }

    class ComponentJson extends EntityJson {
        String componentName;
        String componentVersionName;
        boolean ignored;
        List<String> usages = new ArrayList<>();
        List<OriginJson> origins = new ArrayList<>();
        List<LicenseJson> licenses = new ArrayList<>();

        @Override
        public String toString() {
            return componentName + ' ' + componentVersionName;
        }
    }

    class OriginJson {
        private static final Map<String, String> TYPE_MAPPING = new HashMap<>();

        static {
            TYPE_MAPPING.put("", "generic");
            TYPE_MAPPING.put("alpine", "alpine");
            TYPE_MAPPING.put("bitbucket", "bitbucket");
            TYPE_MAPPING.put("cargo", "cargo");
            TYPE_MAPPING.put("centos", "rpm");
            TYPE_MAPPING.put("composer", "composer");
            TYPE_MAPPING.put("debian", "deb");
            TYPE_MAPPING.put("docker", "docker");
            TYPE_MAPPING.put("gem", "gem");
            TYPE_MAPPING.put("github", "github");
            TYPE_MAPPING.put("golang", "golang");
            TYPE_MAPPING.put("hex", "hex");
            TYPE_MAPPING.put("long_tail", "generic");
            TYPE_MAPPING.put("maven", "maven");
            TYPE_MAPPING.put("npmjs", "npm");
            TYPE_MAPPING.put("nuget", "nuget");
            TYPE_MAPPING.put("pypi", "pypi");
        }

        URI origin;
        String externalNamespace;
        String externalId;

        String getType() {
            final var type = TYPE_MAPPING.get(externalNamespace);
            return (type != null) ? type : "generic";
        }

        String getNamespace() {
            return endPart(2);
        }

        String getName() {
            return endPart(1);
        }

        String getVersion() {
            return endPart(0);
        }

        private String endPart(int offset) {
            final var parts = externalId.split(String.valueOf(separator()));
            return (offset < parts.length) ? parts[parts.length - offset - 1] : "";
        }

        private char separator() {
            switch (externalNamespace) {
                case "maven":
                    return ':';
                default:
                    return '/';
            }
        }

        UUID getComponentId() {
            return uuidFromOrigin(-4);
        }

        UUID getComponentVersion() {
            return uuidFromOrigin(-2);
        }

        UUID getId() {
            return uuidFromOrigin(0);
        }

        private UUID uuidFromOrigin(int fromEnd) {
            final var parts = origin.getPath().split("/");
            return UUID.fromString(parts[parts.length + fromEnd - 1]);
        }
    }

    class DependencyJson extends EntityJson {
        String componentName;
        String versionName;
        String originName; // Like "maven"
        String OriginId; // Like "group:name:version"
    }

    class LicenseJson extends EntityJson {
        String licenseDisplay;
        String licenseType;
        String spdxId;
        List<LicenseJson> licenses = new ArrayList<>();
    }
}

