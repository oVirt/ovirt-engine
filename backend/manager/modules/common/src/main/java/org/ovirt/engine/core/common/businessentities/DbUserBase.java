package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DbUserBase")
public class DbUserBase extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 7161103320297970617L;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    public DbUserBase() {
    }
}
