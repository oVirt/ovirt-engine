package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

public class WebadminRunOnceModel extends RunOnceModel {

    public WebadminRunOnceModel(VM vm, ArrayList<String> customPropertiesKeysList, ICommandTarget commandTarget) {
        super(vm, customPropertiesKeysList, commandTarget);
    }

    @Override
    public void init() {
        super.init();

        getIsAutoAssign().setEntity(true);

        // Custom Properties
        getCustomPropertySheet().setKeyValueString(customPropertiesKeysList);
        getCustomPropertySheet().setEntity(vm.getCustomProperties());

        loadHosts();
    }

    /**
     * Load active hosts bound to active cluster.
     */
    private void loadHosts() {
        // append just active hosts
        AsyncDataProvider.GetHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                final List<VDS> hosts = (ArrayList<VDS>) returnValue;
                final List<VDS> activeHosts = new ArrayList<VDS>();
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
            }
        }), vm.getVdsGroupName());
    }

    @Override
    protected RunVmOnceParams createRunVmOnceParams() {
        RunVmOnceParams params = super.createRunVmOnceParams();

        if ((Boolean) getIsAutoAssign().getEntity()) {
            params.setDestinationVdsId(null);
        } else {
            // set destination host if specified
            VDS defaultHost = (VDS) getDefaultHost().getSelectedItem();
            params.setDestinationVdsId(defaultHost != null ? defaultHost.getId() : null);
        }

        // Sysprep params
        if (getSysPrepDomainName().getSelectedItem() != null)
        {
            params.setSysPrepDomainName(getSysPrepSelectedDomainName().getEntity().equals("") ? //$NON-NLS-1$
                    (String) getSysPrepSelectedDomainName().getEntity()
                    : (String) getSysPrepDomainName().getSelectedItem());
        }

        return params;
    }

    @Override
    protected void onRunOnce() {
        Frontend.RunAction(VdcActionType.RunVmOnce, createRunVmOnceParams(), null, this);
        commandTarget.executeCommand(runOnceCommand);
    }
}
