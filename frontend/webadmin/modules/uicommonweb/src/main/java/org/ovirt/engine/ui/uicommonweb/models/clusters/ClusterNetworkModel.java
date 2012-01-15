package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;

@SuppressWarnings("unused")
public class ClusterNetworkModel extends NetworkModel
{
    private String dataCenterName;

    public String getDataCenterName()
    {
        return dataCenterName;
    }

    public void setDataCenterName(String value)
    {
        if (!StringHelper.stringsEqual(dataCenterName, value))
        {
            dataCenterName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DataCenterName"));
        }
    }
}
