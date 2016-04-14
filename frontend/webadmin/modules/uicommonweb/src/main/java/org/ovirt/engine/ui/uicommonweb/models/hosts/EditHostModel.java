package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class EditHostModel extends HostModel {

    public EditHostModel() {
        getExternalHostProviderEnabled().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
    }

    @Override
    protected boolean showInstallationProperties() {
        return false;
    }

    @Override
    protected void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds) {
        if (dataCenters != null) {
            getDataCenter().setItems(dataCenters);
            getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters,
                    new Linq.IdPredicate<>(vds.getStoragePoolId())));
            if (getDataCenter().getSelectedItem() == null) {
                getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));
            }
        }
    }

    @Override
    protected void setAllowChangeHost(VDS vds) {
        if (vds.getStatus() != VDSStatus.InstallFailed) {
            getHost().setIsChangeable(false);
            getAuthSshPort().setIsChangeable(false);
        } else {
            getHost().setIsChangeable(true);
            getAuthSshPort().setIsChangeable(true);
        }
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        getDataCenter().setChangeProhibitionReason(constants.dcCanOnlyBeChangedWhenHostInMaintMode());
        getDataCenter().setIsChangeable(false);
        getCluster().setChangeProhibitionReason(constants.clusterCanOnlyBeChangedWhenHostInMaintMode());
        getCluster().setIsChangeable(false);
    }

    @Override
    protected void updateProvisionedHosts() {
    }

    @Override
    public boolean showExternalProviderPanel() {
        return true;
    }

    @Override
    public boolean externalProvisionEnabled() {
        return false;
    }

    @Override
    protected void setPort(VDS vds) {
        getPort().setEntity(vds.getPort());
    }

    @Override
    protected void updateModelClusterFromVds(ArrayList<Cluster> clusters, VDS vds) {
        if (clusters != null) {
            getCluster().setSelectedItem(Linq.firstOrNull(clusters,
                    new Linq.IdPredicate<>(vds.getClusterId())));
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

    public void setSelectedCluster(VDS host) {
        ArrayList<Cluster> clusters;
        if (getCluster().getItems() == null) {
            Cluster tempVar = new Cluster();
            tempVar.setName(host.getClusterName());
            tempVar.setId(host.getClusterId());
            tempVar.setCompatibilityVersion(host.getClusterCompatibilityVersion());
            getCluster()
                    .setItems(new ArrayList<>(Arrays.asList(new Cluster[]{tempVar})));
        }
        clusters = (ArrayList<Cluster>) getCluster().getItems();
        updateModelClusterFromVds(clusters, host);
        if (getCluster().getSelectedItem() == null) {
            getCluster().setSelectedItem(Linq.firstOrNull(clusters));
        }
    }

    @Override
    protected void cpuVendorChanged() {
        updateKernelCmdlineCheckboxesChangeability();
    }
}
