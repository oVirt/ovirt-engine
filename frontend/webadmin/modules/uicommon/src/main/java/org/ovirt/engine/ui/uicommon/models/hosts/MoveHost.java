package org.ovirt.engine.ui.uicommon.models.hosts;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class MoveHost extends ListModel
{

	private ListModel privateCluster;
	public ListModel getCluster()
	{
		return privateCluster;
	}
	private void setCluster(ListModel value)
	{
		privateCluster = value;
	}

	private java.util.ArrayList<VDS> privateSelectedHosts;
	public java.util.ArrayList<VDS> getSelectedHosts()
	{
		return privateSelectedHosts;
	}
	public void setSelectedHosts(java.util.ArrayList<VDS> value)
	{
		privateSelectedHosts = value;
	}


	public MoveHost()
	{
		setCluster(new ListModel());
		//Cluster.ValueChanged += new EventHandler(Cluster_ValueChanged);
		getCluster().getSelectedItemChangedEvent().addListener(this);
	}

	private void Cluster_SelectedItemChanged()
	{
		if (getCluster().getSelectedItem() != null)
		{
			VDSGroup cluster = (VDSGroup)getCluster().getSelectedItem();
			//IEnumerable<VDSGroup> clusters = Cluster.Options.Cast<VDSGroup>();

			java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>)getCluster().getItems();
			//var hosts = DataProvider.GetHostList()
			//.Where(a => clusters.All(b => b.ID != a.vds_group_id)
			//     && (a.status == VDSStatus.Maintenance || a.status == VDSStatus.PendingApproval)
			//    && (a.Version.FullVersion == null || a.Version.FullVersion.GetFriendlyVersion() >= cluster.compatibility_version))
			//.ToList();
			//var items = hosts.Select(a => new EntityModel() { Entity = a }).ToList();

			java.util.ArrayList<VDS> hosts = DataProvider.GetHostList();
			java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
			for (VDS vds : hosts)
			{
				if (Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(vds.getvds_group_id())) == null && (vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval) && (vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion().getFullVersion()).compareTo(cluster.getcompatibility_version()) >= 0))
				{
					EntityModel entity = new EntityModel();
					entity.setEntity(vds);
					items.add(entity);
				}
			}


			//IEnumerable<int> previouslySelectedHostIDs = new List<int>();
			java.util.ArrayList<Guid> previouslySelectedHostIDs = new java.util.ArrayList<Guid>();
			if (getItems() != null)
			{
				//previouslySelectedHostIDs =
				//    Items.Cast<EntityModel>().Where(a => Selector.GetIsSelected(a)).Select(a => (a.Entity as VDS).vds_id);
				for (Object item : getItems())
				{
					EntityModel entity = (EntityModel)item;
					if (entity.getIsSelected())
					{
						previouslySelectedHostIDs.add(((VDS)entity.getEntity()).getvds_id());
					}
				}
			}
			setItems(items);
			//items.Each(a => Selector.SetIsSelected(a, previouslySelectedHostIDs.Contains((a.Entity as VDS).vds_id)));
			for (EntityModel entity : items)
			{
				entity.setIsSelected(previouslySelectedHostIDs.contains(((VDS)entity.getEntity()).getvds_id()));
			}
		}
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(SelectedItemChangedEventDefinition) && sender == getCluster())
		{
			Cluster_SelectedItemChanged();
		}
	}

	public boolean Validate()
	{
		getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getCluster().getIsValid();
	}
}