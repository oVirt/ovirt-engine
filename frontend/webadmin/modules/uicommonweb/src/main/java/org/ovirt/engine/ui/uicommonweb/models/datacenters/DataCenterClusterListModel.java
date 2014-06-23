package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class DataCenterClusterListModel extends ClusterListModel
{
    @Inject
    public DataCenterClusterListModel(ClusterVmListModel clusterVmListModel,
            ClusterServiceModel clusterServiceModel,
            ClusterGlusterHookListModel clusterGlusterHookListModel,
            ClusterAffinityGroupListModel clusterAffinityGroupListModel,
            CpuProfileListModel cpuProfileListModel,
            ClusterGeneralModel clusterGeneralModel,
            ClusterNetworkListModel clusterNetworkListModel,
            ClusterHostListModel clusterHostListModel,
            PermissionListModel permissionListModel) {
        super(clusterVmListModel,
                clusterServiceModel,
                clusterGlusterHookListModel,
                clusterAffinityGroupListModel,
                cpuProfileListModel,
                clusterGeneralModel,
                clusterNetworkListModel,
                clusterHostListModel,
                permissionListModel);
    }

    @Override
    public StoragePool getEntity()
    {
        return (StoragePool) ((super.getEntity() instanceof StoragePool) ? super.getEntity() : null);
    }

    public void setEntity(StoragePool value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            setSearchString("clusters: datacenter.name=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().execute();
        }
    }
}
