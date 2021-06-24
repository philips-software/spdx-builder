/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class OrtReaderTest {

  private static final File ORT_SAMPLE1 = getPathOfFile("ort_sample1.yml");
  private static final File ORT_SAMPLE2 = getPathOfFile("ort_sample2.yml");
  private final BillOfMaterials bom = new BillOfMaterials();

  static File getPathOfFile(String fileName) {
	return Path.of("src", "test", "resources", fileName).toFile();
  }

  void createBOM(File file) {
	OrtReader ortSample = new OrtReader(file);
	ortSample.defineProjectPackage("NPM::mime-types:2.1.18", URI.create("pkg:npm/mime-types@2.1.18"))
	  .excludeScopes("NPM::mime-types:2.1.18", List.of("test*"))
	  .read(bom);
  }

  @Test
  void loadsOrtSample() {
	createBOM(ORT_SAMPLE1);
	assertThat(bom.getPackages()).hasSize(1 + 2);
  }

  @Test()
  void abortConversionOnIssues() {
	Exception exception = assertThrows(OrtReaderException.class, () ->
	  createBOM(ORT_SAMPLE2)
	);

	String expectedMessage = "The analyzed ORT file has issues, unable to generate a valid SPDX file";
	String actualMessage = exception.getMessage();

	assertTrue(actualMessage.contains(expectedMessage));
  }
}
