package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.uicommonweb.Linq;

public class EditHostModel extends HostModel {

    public EditHostModel() {
        super();
    }

    @Override
    protected boolean showInstallationProperties() {
        return false;
    }

    @Override
    protected void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds) {
        if (dataCenters != null)
        {
            getDataCenter().setItems(dataCenters);
            getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters,
                    new Linq.DataCenterPredicate(vds.getStoragePoolId())));
            if (getDataCenter().getSelectedItem() == null) {
                getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));
            }
        }
    }

    @Override
    protected void setAllowChangeHost(VDS vds) {
        if (vds.getStatus() != VDSStatus.InstallFailed) {
            getHost().setIsChangable(false);
            getPort().setIsChangable(false);
            getFetchSshFingerprint().setIsChangable(false);
        } else {
            getHost().setIsChangable(true);
            getPort().setIsChangable(true);
            getFetchSshFingerprint().setIsChangable(true);
        }
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        getDataCenter().setChangeProhibitionReason("Data Center can be changed only when the Host is in Maintenance mode."); //$NON-NLS-1$
        getDataCenter().setIsChangable(false);
        getCluster().setChangeProhibitionReason("Cluster can be changed only when the Host is in Maintenance mode."); //$NON-NLS-1$
        getCluster().setIsChangable(false);
    }

    @Override
    protected void updateHosts() {
    }

    @Override
    public boolean showExternalProviderPanel() {
        return false;
    }

    @Override
    protected void setHostPort(VDS vds) {
        getPort().setEntity(vds.getPort());
    }

    @Override
    protected void updateModelClusterFromVds(ArrayList<VDSGroup> clusters, VDS vds) {
        if (clusters != null) {
            getCluster().setSelectedItem(Linq.firstOrDefault(clusters,
                    new Linq.ClusterPredicate(vds.getVdsGroupId())));
        }
    }

}
