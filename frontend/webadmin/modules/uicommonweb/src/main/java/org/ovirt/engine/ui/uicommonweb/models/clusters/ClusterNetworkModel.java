package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;

@SuppressWarnings("unused")
public class ClusterNetworkModel extends NetworkModel
{

    public ClusterNetworkModel() {
        setMtu(new EntityModel());
        setIsVmNetwork(new EntityModel());
    }

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

    // MTU

    private EntityModel privateMtu;

    public EntityModel getMtu()
    {
        return privateMtu;
    }

    private void setMtu(EntityModel value)
    {
        privateMtu = value;
    }

    // VM Network

    private EntityModel privateIsVmNetwork;

    public EntityModel getIsVmNetwork()
    {
        return privateIsVmNetwork;
    }

    private void setIsVmNetwork(EntityModel value)
    {
        privateIsVmNetwork = value;
    }

}
