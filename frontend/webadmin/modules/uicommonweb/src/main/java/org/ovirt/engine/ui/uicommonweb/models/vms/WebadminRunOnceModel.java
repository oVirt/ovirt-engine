package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

public class WebadminRunOnceModel extends RunOnceModel {

    public WebadminRunOnceModel(VM vm, ICommandTarget commandTarget) {
        super(vm, commandTarget);
    }

    @Override
    public void init() {
        super.init();

        getIsAutoAssign().setEntity(true);

        // Custom Properties
        getCustomPropertySheet().setKeyValueMap(
                AsyncDataProvider.getInstance().getCustomPropertiesList().get(vm.getCompatibilityVersion()));
        getCustomPropertySheet().deserialize(vm.getCustomProperties());

        loadHosts();
    }

    /**
     * Load active hosts bound to active cluster.
     */
    private void loadHosts() {
        // append just active hosts
        AsyncDataProvider.getInstance().getHostListByCluster(new AsyncQuery<>(
                hosts -> {
                    final List<VDS> activeHosts = new ArrayList<>();
                    for (VDS host : hosts) {
                        if (VDSStatus.Up.equals(host.getStatus())) {
                            activeHosts.add(host);
                        }
                    }

                    getDefaultHost().setItems(activeHosts);

                    // hide host tab when no active host is available
                    if (activeHosts.isEmpty()) {
                        setIsHostTabVisible(false);
                    }
                }), vm.getClusterName());
    }

    @Override
    protected RunVmOnceParams createRunVmOnceParams() {
        RunVmOnceParams params = super.createRunVmOnceParams();

        if (getIsAutoAssign().getEntity()) {
            params.setDestinationVdsId(null);
        } else {
            // set destination host if specified
            VDS defaultHost = getDefaultHost().getSelectedItem();
            params.setDestinationVdsId(defaultHost != null ? defaultHost.getId() : null);
        }

        return params;
    }

    @Override
    protected void onRunOnce() {
        Frontend.getInstance().runAction(ActionType.RunVmOnce, createRunVmOnceParams(), null, this);
        commandTarget.executeCommand(runOnceCommand);
    }
}
