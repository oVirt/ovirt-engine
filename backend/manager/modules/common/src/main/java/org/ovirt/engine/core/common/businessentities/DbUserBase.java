package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

public class DbUserBase extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 7161103320297970617L;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
    }

    public DbUserBase() {
    }
}
