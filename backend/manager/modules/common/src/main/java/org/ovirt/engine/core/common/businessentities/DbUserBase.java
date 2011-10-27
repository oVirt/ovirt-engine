package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DbUserBase")
public class DbUserBase extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 7161103320297970617L;

    // C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    public DbUserBase() {
    }
}
