package com.philips.research.convert2spdx.persistence.ort;

import java.net.URL;
import java.util.List;

public class OrtJson {
    AnalyzerJson analyzer;
}

class AnalyzerJson {
    ResultJson result;
}

class ResultJson {
    List<PackageJson> projects;
    List<PackageJson> packages;
    boolean has_issues;
}

class PackageJson {
    String id;

    List<String> declared_licenses;
    LocationJson source_artifact;
}

class LocationJson {
    URL url;
    String hash;
    String hash_algorithm;
}
