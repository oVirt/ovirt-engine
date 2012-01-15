package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;

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

    private java.util.ArrayList<VDSGroup> privatenewClusters;

    public java.util.ArrayList<VDSGroup> getnewClusters()
    {
        return privatenewClusters;
    }

    public void setnewClusters(java.util.ArrayList<VDSGroup> value)
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

    private java.util.ArrayList<SelectionTreeNodeModel> privateclusterTreeNodes;

    public java.util.ArrayList<SelectionTreeNodeModel> getclusterTreeNodes()
    {
        return privateclusterTreeNodes;
    }

    public void setclusterTreeNodes(java.util.ArrayList<SelectionTreeNodeModel> value)
    {
        privateclusterTreeNodes = value;
    }

    public java.util.ArrayList<SelectionTreeNodeModel> getClusterTreeNodes()
    {
        return getclusterTreeNodes();
    }

    public void setClusterTreeNodes(java.util.ArrayList<SelectionTreeNodeModel> value)
    {
        setclusterTreeNodes(value);
        OnPropertyChanged(new PropertyChangedEventArgs("ClusterTreeNodes"));
    }

    private java.util.ArrayList<VDSGroup> privateOriginalClusters;

    public java.util.ArrayList<VDSGroup> getOriginalClusters()
    {
        return privateOriginalClusters;
    }

    public void setOriginalClusters(java.util.ArrayList<VDSGroup> value)
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
        super();
        setClusterTreeNodes(new java.util.ArrayList<SelectionTreeNodeModel>());
        setOriginalClusters(new java.util.ArrayList<VDSGroup>());
        setIsEnabled(new EntityModel());
        getIsEnabled().setEntity(true);
        setDetachAllAvailable(new EntityModel());
        getDetachAllAvailable().setEntity(false);
    }
}
