package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.INotifyPropertyChanged;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmBase")
public class VmBase extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 1078548170257965614L;

    public VmBase() {
        mOs = VmOsType.Unassigned;
    }

    private VmOsType mOs = VmOsType.Unassigned;

    @XmlElement
    public VmOsType getos() {
        return mOs;
    }

    public void setos(VmOsType value) {
        mOs = value;
    }

    @Deprecated
    public VmOsType getOsType() {
        return getos();
    }

    @Deprecated
    public void setOsType(VmOsType value) {
        setos(value);
    }
}
