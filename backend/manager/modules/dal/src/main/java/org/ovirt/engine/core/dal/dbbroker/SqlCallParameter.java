package org.ovirt.engine.core.dal.dbbroker;

public class SqlCallParameter {
    private int ordinal;
    private String name;
    private int dataType;

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public SqlCallParameter(int ordinal, String name, int dataType) {
        super();
        this.ordinal = ordinal;
        this.name = name;
        this.dataType = dataType;
    }

}
