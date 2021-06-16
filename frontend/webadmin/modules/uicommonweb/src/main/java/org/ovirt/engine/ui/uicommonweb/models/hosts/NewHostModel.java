package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;

public class NewHostModel extends HostModel {

    public static final int NewHostDefaultPort = 54321;

    public NewHostModel() {
        getExternalHostProviderEnabled().setIsAvailable(false);
        getProviders().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            // While load don't let user to change provider
            getProviders().setIsChangeable(false);
        });

        getProviders().setIsAvailable(false);
        getExternalHostProviderEnabled().setEntity(false);
    }

    // Define events:
    @Override
    protected boolean showInstallationProperties() {
        return true;
    }

    @Override
    protected void updateModelDataCenterFromVds(List<StoragePool> dataCenters, VDS vds) {
    }

    @Override
    protected void setAllowChangeHost(VDS vds) {
        getHost().setIsChangeable(getHost().getEntity() == null);
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        getDataCenter().setIsChangeable(true);
        getCluster().setIsChangeable(true);
    }

    @Override
    public boolean showExternalProviderPanel() {
        return false;
    }

    @Override
    protected void setPort(VDS vds) {
        // If port is "0" then we set it to the default port
        if (vds.getPort() == 0) {
            getPort().setEntity(NewHostDefaultPort);
        }
    }

    @Override
    protected void updateModelClusterFromVds(ArrayList<Cluster> clusters, VDS vds) {
    }

    @Override
    protected boolean editTransportProperties(VDS vds) {
        return true;
    }

    protected void cpuVendorChanged() {
        resetKernelCmdline();
    }
}
