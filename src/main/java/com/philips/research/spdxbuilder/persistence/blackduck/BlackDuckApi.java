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
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Black Duck REST API declaration for Retrofit2
 */
public interface BlackDuckApi {
    @Headers({"Accept: application/vnd.blackducksoftware.user-4+json"})
    @POST("tokens/authenticate")
    Call<AuthResponse> authenticate(@Header("Authorization") String authHeader);

    @Headers({"Accept: application/vnd.blackducksoftware.status-4+json"})
    @GET("current-version")
    Call<VersionResponse> serverVersion();

    class AuthResponse {
        String bearerToken;
    }

    class VersionResponse {
        String version;
    }
}

