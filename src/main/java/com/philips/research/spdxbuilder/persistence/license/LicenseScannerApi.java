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

import com.fasterxml.jackson.annotation.JsonInclude;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.net.URI;

/**
 * Retrofit REST API declaration.
 */
interface LicenseScannerApi {
    /**
     * Start scanning a package or retrieve result from an earlier scan.
     *
     * @return scan result with or without a concluded license
     */
    @POST("/packages")
    Call<ResultJson> scan(@Body RequestJson body);

    /**
     * Contests an existing scan.
     *
     * @param scanId UUID of the scan.
     */
    @POST("/scans/{scanId}/contest")
    Call<Void> contest(@Path("scanId") String scanId, @Body ContestJson body);


    class RequestJson {
        URI purl;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @NullOr String location;

        public RequestJson(URI purl, @NullOr URI location) {
            this.purl = purl;
            if (location != null) {
                this.location = location.toASCIIString();
            }
        }
    }

    class ContestJson {
        String license;

        public ContestJson(String license) {
            this.license = license;
        }
    }
}

