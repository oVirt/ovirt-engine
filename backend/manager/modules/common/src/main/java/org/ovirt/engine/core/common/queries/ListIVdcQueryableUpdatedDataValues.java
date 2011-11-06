package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import java.util.ArrayList;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
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

    @XmlElement
    public ArrayList<ListIVdcQueryableUpdatedData> getValue() {
        return value;
    }

    public void setValue(ArrayList<ListIVdcQueryableUpdatedData> value) {
        this.value = value;
    }

    @XmlElement
    public String getCsharpworkaround() {
        return csharpworkaround;
    }

    public void setCsharpworkaround(String csharpworkaround) {
        this.csharpworkaround = csharpworkaround;
    }
}
