package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ChangeDiskCommandParameters")
public class ChangeDiskCommandParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 2876214350273132268L;
    @XmlElement
    private String _cdImagePath;

    public ChangeDiskCommandParameters(Guid vmId, String cdImagePath) {
        super(vmId);
        _cdImagePath = cdImagePath;
    }

    public String getCdImagePath() {
        return _cdImagePath;
    }

    public ChangeDiskCommandParameters() {
    }
}
