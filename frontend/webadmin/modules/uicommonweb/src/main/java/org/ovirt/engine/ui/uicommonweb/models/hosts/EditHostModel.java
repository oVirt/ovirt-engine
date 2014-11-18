package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

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
            getAuthSshPort().setIsChangable(false);
        } else {
            getHost().setIsChangable(true);
            getAuthSshPort().setIsChangable(true);
        }
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        getDataCenter().setChangeProhibitionReason(constants.dcCanOnlyBeChangedWhenHostInMaintMode());
        getDataCenter().setIsChangable(false);
        getCluster().setChangeProhibitionReason(constants.clusterCanOnlyBeChangedWhenHostInMaintMode());
        getCluster().setIsChangable(false);
    }

    @Override
    public void updateHosts() {
    }

    @Override
    protected void updateProvisionedHosts() {
    }

    @Override
    public boolean showExternalProviderPanel() {
        return false;
    }

    @Override
    protected void setPort(VDS vds) {
        getPort().setEntity(vds.getPort());
    }

    @Override
    protected void updateModelClusterFromVds(ArrayList<VDSGroup> clusters, VDS vds) {
        if (clusters != null) {
            getCluster().setSelectedItem(Linq.firstOrDefault(clusters,
                    new Linq.ClusterPredicate(vds.getVdsGroupId())));
        }
    }

    @Override
    public boolean showNetworkProviderTab() {
        return false;
    }

    @Override
    protected boolean editTransportProperties(VDS vds) {
        if (VDSStatus.Maintenance.equals(vds.getStatus())) {
            return true;
        }
        return false;
    }

}
