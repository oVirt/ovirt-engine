package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class DataCenterNetworkModel extends NetworkModel
{
    private network privatecurrentNetwork;

    public network getcurrentNetwork()
    {
        return privatecurrentNetwork;
    }

    public void setcurrentNetwork(network value)
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

    private UICommand privateDetachAllCommand;

    public UICommand getDetachAllCommand()
    {
        return privateDetachAllCommand;
    }

    public void setDetachAllCommand(UICommand value)
    {
        privateDetachAllCommand = value;
    }

    private EntityModel privateDetachAllAvailable;

    public EntityModel getDetachAllAvailable()
    {
        return privateDetachAllAvailable;
    }

    public void setDetachAllAvailable(EntityModel value)
    {
        privateDetachAllAvailable = value;
    }

    private ArrayList<SelectionTreeNodeModel> privateclusterTreeNodes;

    public ArrayList<SelectionTreeNodeModel> getclusterTreeNodes()
    {
        return privateclusterTreeNodes;
    }

    public void setclusterTreeNodes(ArrayList<SelectionTreeNodeModel> value)
    {
        privateclusterTreeNodes = value;
    }

    public ArrayList<SelectionTreeNodeModel> getClusterTreeNodes()
    {
        return getclusterTreeNodes();
    }

    public void setClusterTreeNodes(ArrayList<SelectionTreeNodeModel> value)
    {
        setclusterTreeNodes(value);
        OnPropertyChanged(new PropertyChangedEventArgs("ClusterTreeNodes")); //$NON-NLS-1$
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
        setClusterTreeNodes(new ArrayList<SelectionTreeNodeModel>());
        setOriginalClusters(new ArrayList<VDSGroup>());
        setIsEnabled(new EntityModel());
        getIsEnabled().setEntity(true);
        setDetachAllAvailable(new EntityModel());
        getDetachAllAvailable().setEntity(false);
    }

}
