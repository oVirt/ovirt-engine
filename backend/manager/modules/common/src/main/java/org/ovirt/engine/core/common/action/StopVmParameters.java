package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StopVmParameters")
public class StopVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -1331508207367552128L;
    @XmlElement
    private StopVmTypeEnum _stopVmType = StopVmTypeEnum.forValue(0);

    public StopVmParameters(Guid vmID, StopVmTypeEnum stopVmType) {
        super(vmID);
        _stopVmType = stopVmType;
    }

    public StopVmTypeEnum getStopVmType() {
        return _stopVmType;
    }

    public void setStopVmType(StopVmTypeEnum value) {
        _stopVmType = value;
    }

    public StopVmParameters() {
    }
}
