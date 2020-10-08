/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.UUID;

/**
 * Retrofit REST API declaration.
 */
interface LicenseScannerApi {
    /**
     * Start scanning a package or retrieve result from an earlier scan.
     *
     * @return scan result with or without a concluded license
     */
    @POST("/packages/{namespace}/{name}/{version}")
    Call<ResultJson> scan(@Path("namespace") String namespace,
                          @Path("name") String name,
                          @Path("version") String version,
                          @Body RequestJson body);

    /**
     * Contests an existing scan.
     *
     * @param scanId UUID of the scan.
     */
    @POST("/scans/{scan}/contest")
    Call<Void> contest(@Path("scan") UUID scanId);
}
