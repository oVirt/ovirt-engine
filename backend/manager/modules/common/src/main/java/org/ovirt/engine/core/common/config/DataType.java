package org.ovirt.engine.core.common.config;

public enum DataType {
    String,
    Int,
    Bool,
    DateTime,
    TimeSpan,
    Version,
    Map;
    public int getValue() {
        return this.ordinal();
    }
}
