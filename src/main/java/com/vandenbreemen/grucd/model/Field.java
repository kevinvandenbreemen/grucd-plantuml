package com.vandenbreemen.grucd.model;

public class Field {

    private String name;
    private String typeName;

    public Field(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }
}
