package org.ovirt.engine.ui.uicommon.models.datacenters;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

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

	public java.util.ArrayList<VDSGroup> getClusters()
	{
		return null;
	}
	public void setClusters(java.util.ArrayList<VDSGroup> value)
	{
		SelectionTreeNodeModel nodeModel;
		for (VDSGroup selectionTreeNodeModel : value)
		{
			nodeModel = new SelectionTreeNodeModel();
			nodeModel.setEntity(selectionTreeNodeModel);
			nodeModel.setDescription(selectionTreeNodeModel.getname());
			nodeModel.setIsSelectedNullable(false);
			getClusterTreeNodes().add(nodeModel);
		}
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