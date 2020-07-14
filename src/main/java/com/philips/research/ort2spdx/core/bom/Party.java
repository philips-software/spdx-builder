package com.philips.research.ort2spdx.core.bom;

enum Type {NONE, PERSON, ORGANIZATION}

public class Party {
    private final Type type;
    private final String name;

    public Party() {
        this(Type.NONE, "");
    }

    public Party(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
