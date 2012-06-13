package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

@SuppressWarnings("unused")
public class DataCenterNetworkModel extends NetworkModel
{
    private Network privatecurrentNetwork;

    public Network getcurrentNetwork()
    {
        return privatecurrentNetwork;
    }

    public void setcurrentNetwork(Network value)
    {
        privatecurrentNetwork = value;
    }

    private ArrayList<VDSGroup> privatenewClusters;

    public ArrayList<VDSGroup> getnewClusters()
    {
        return privatenewClusters;
    }

    public void setnewClusters(ArrayList<VDSGroup> value)
    {
        privatenewClusters = value;
    }

    private UICommand privateApplyCommand;

    public UICommand getApplyCommand()
    {
        return privateApplyCommand;
    }

    public void setApplyCommand(UICommand value)
    {
        privateApplyCommand = value;
    }

    private ListModel privateNetworkClusterList;

    public ListModel getNetworkClusterList()
    {
        return privateNetworkClusterList;
    }

    public void setNetworkClusterList(ListModel value)
    {
        privateNetworkClusterList = value;
        OnPropertyChanged(new PropertyChangedEventArgs("NetworkClusterList")); //$NON-NLS-1$
    }

    private ArrayList<VDSGroup> privateOriginalClusters;

    public ArrayList<VDSGroup> getOriginalClusters()
    {
        return privateOriginalClusters;
    }

    public void setOriginalClusters(ArrayList<VDSGroup> value)
    {
        privateOriginalClusters = value;
    }

    private EntityModel privateIsEnabled;

    public EntityModel getIsEnabled()
    {
        return privateIsEnabled;
    }

    public void setIsEnabled(EntityModel value)
    {
        privateIsEnabled = value;
    }

    public DataCenterNetworkModel()
    {
        setNetworkClusterList(new ListModel());
        setOriginalClusters(new ArrayList<VDSGroup>());
        setIsEnabled(new EntityModel());
        getIsEnabled().setEntity(true);
    }

}
