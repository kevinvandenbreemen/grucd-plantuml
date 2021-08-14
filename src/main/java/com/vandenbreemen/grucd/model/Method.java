package com.vandenbreemen.grucd.model;

public class Method {

    private String name;
    private String returnType;

    public Method(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }
}
