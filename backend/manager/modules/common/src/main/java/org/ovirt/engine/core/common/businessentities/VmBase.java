package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmBase")
public class VmBase extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 1078548170257965614L;

    public VmBase() {
        mOs = VmOsType.Unassigned;
    }

    private VmOsType mOs = VmOsType.forValue(0);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public VmOsType getos() {
        // if (/*string.IsNullOrEmpty(mOs) &&*/ mVmOsType != VmOsType.Other)
        // {
        // mOs = mVmOsType/*.toString()*/;
        // }
        return mOs;
    }

    public void setos(VmOsType value) {
        mOs = value;
        OnPropertyChanged(new PropertyChangedEventArgs("os"));
    }

    @Deprecated
    public VmOsType getOsType() {
        return getos();
    }

    @Deprecated
    public void setOsType(VmOsType value) {
        setos(value);
    }

    // C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

}
