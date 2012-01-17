package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.action.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddPowerClientParameters")
public class AddPowerClientParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3805094506832541836L;

    public AddPowerClientParameters(AddVdsActionParameters AddVdsParams) {
        _AddVdsParams = AddVdsParams;
    }

    @XmlElement(name = "AddVdsParams")
    private AddVdsActionParameters _AddVdsParams;

    public AddVdsActionParameters getAddVdsParams() {
        return _AddVdsParams;
    }

    public AddPowerClientParameters() {
    }
}
