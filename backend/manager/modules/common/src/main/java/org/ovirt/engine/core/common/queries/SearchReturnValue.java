package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SearchReturnValue", namespace = "http://service.engine.ovirt.org")
public class SearchReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = -3741619784123689926L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private boolean _validSearch;

    public boolean getIsSearchValid() {
        return _validSearch;
    }

    public void setIsSearchValid(boolean value) {
        _validSearch = value;
    }

    public SearchReturnValue() {
    }
}
