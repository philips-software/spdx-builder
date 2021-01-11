/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.persistence.license.LicenseScannerException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

import java.io.File;
import java.io.IOException;
import java.net.URI;

interface UploadApi {
    @Multipart
    @POST
    Call<Void> uploadFile(@Url String path, @Part MultipartBody.Part filePart);
}

public class UploadClient {
    private final UploadApi rest;
    private final URI uploadUrl;

    UploadClient(URI uploadUrl) {
        this.uploadUrl = uploadUrl;
        var uploadPath = uploadUrl.toASCIIString();
        if (!uploadPath.endsWith("/")) {
           uploadPath += '/' ;
        }
        final var retrofit = new Retrofit.Builder()
                .baseUrl(uploadPath)
                .build();
        rest = retrofit.create(UploadApi.class);
    }

    void upload(File file) {
        try {
            System.out.println("Uploading '" + file.getName() + "' to " + uploadUrl);
            final var reqBody = RequestBody.create(MediaType.parse("text/plain;charset=UTF-8"), file);
            final var filePart = MultipartBody.Part.createFormData("file", "sbom.spdx", reqBody);
            final var response = rest.uploadFile(uploadUrl.getPath(), filePart).execute();
            if (!response.isSuccessful()) {
                throw new BusinessException("SPDX upload responded with status " + response.code());
            }
        } catch (IOException e) {
            throw new LicenseScannerException("The SPDX upload server is not reachable at " + uploadUrl);
        }
    }
}
