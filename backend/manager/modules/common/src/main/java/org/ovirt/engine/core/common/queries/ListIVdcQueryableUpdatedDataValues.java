package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

public class ListIVdcQueryableUpdatedDataValues {
    private ArrayList<ListIVdcQueryableUpdatedData> value;
    private String csharpworkaround; // without this, C# wsdl processing will
                                     // auto-convert this class to [] and
                                     // then fail

    public ListIVdcQueryableUpdatedDataValues() {
    }

    public ListIVdcQueryableUpdatedDataValues(ArrayList<ListIVdcQueryableUpdatedData> value) {
        this.value = value;
    }

    public ArrayList<ListIVdcQueryableUpdatedData> getValue() {
        return value;
    }

    public void setValue(ArrayList<ListIVdcQueryableUpdatedData> value) {
        this.value = value;
    }

    public String getCsharpworkaround() {
        return csharpworkaround;
    }

    public void setCsharpworkaround(String csharpworkaround) {
        this.csharpworkaround = csharpworkaround;
    }
}
