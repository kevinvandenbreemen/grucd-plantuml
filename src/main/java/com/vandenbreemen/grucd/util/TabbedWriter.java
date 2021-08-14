package com.vandenbreemen.grucd.util;

public class TabbedWriter {

    private StringBuilder bld;
    private String tabPrefix;

    public TabbedWriter(StringBuilder bld) {
        this.bld = bld;
        tabPrefix = "";
    }

    public TabbedWriter() {
        this(new StringBuilder());
    }

    public TabbedWriter tab() {
        tabPrefix += "\t";
        return this;
    }

    public TabbedWriter unTab() {
        tabPrefix = tabPrefix.substring(0, tabPrefix.length() - "\t".length());
        return this;
    }

    public TabbedWriter println(String line) {
        bld.append(tabPrefix).append(line).append("\n");
        return this;
    }

    public String toString() {
        return bld.toString();
    }
}
