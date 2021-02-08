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
import java.util.List;
import java.util.UUID;

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
    @GET("/api/projects/{projectId}/versions/{versionId}/components")
    Call<ItemsJson<ComponentJson>> readComponents(@Path("projectId") UUID projectId, @Path("versionId") UUID versionId);

    @Headers({"Accept: application/vnd.blackducksoftware.component-detail-4+json"})
    @GET("/api/components/{componentId}/versions/{componentVersionId}/origins/{originId}/direct-dependencies")
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
        List<T> items;
    }

    abstract class EntityJson {
        MetaJson _meta;

        UUID getId() {
            final var path = _meta.href.getPath();
            return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
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
        List<LicenseJson> licenses;
        List<OriginJson> origins;
    }

    class OriginJson {
        String name;
        URI origin;
        String externalNamespace;
        String externalId;

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
        List<LicenseJson> licenses;
    }
}

