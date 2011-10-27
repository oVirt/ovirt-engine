package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetConfigurationValueParametes")
public class SetConfigurationValueParametes extends VdcActionParametersBase {
    private static final long serialVersionUID = -4966875942424830052L;

    public SetConfigurationValueParametes(VdcOption option) {
        setOption(option);
    }

    @XmlElement(name = "Option")
    private VdcOption privateOption;

    public VdcOption getOption() {
        return privateOption;
    }

    private void setOption(VdcOption value) {
        privateOption = value;
    }

    public SetConfigurationValueParametes() {
    }
}
