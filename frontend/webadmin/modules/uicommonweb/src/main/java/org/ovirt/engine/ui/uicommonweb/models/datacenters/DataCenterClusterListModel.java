package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class DataCenterClusterListModel extends ClusterListModel<StoragePool> {
    @Inject
    public DataCenterClusterListModel(ClusterVmListModel clusterVmListModel,
            ClusterServiceModel clusterServiceModel,
            ClusterGlusterHookListModel clusterGlusterHookListModel,
            ClusterAffinityGroupListModel clusterAffinityGroupListModel,
            CpuProfileListModel cpuProfileListModel,
            ClusterGeneralModel clusterGeneralModel,
            ClusterNetworkListModel clusterNetworkListModel,
            ClusterHostListModel clusterHostListModel,
            PermissionListModel<Cluster> permissionListModel,
            ClusterAffinityLabelListModel clusterAffinityLabelListModel) {
        super(clusterVmListModel,
                clusterServiceModel,
                clusterGlusterHookListModel,
                clusterAffinityGroupListModel,
                cpuProfileListModel,
                clusterGeneralModel,
                clusterNetworkListModel,
                clusterHostListModel,
                permissionListModel,
                clusterAffinityLabelListModel);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            setSearchString("clusters: datacenter.name=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }
}
