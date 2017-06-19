package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.action.SyncUiAction;
import org.ovirt.engine.ui.uicommonweb.action.UiAction;
import org.ovirt.engine.ui.uicommonweb.action.UiVdcMultipleAction;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class VfsConfigAction extends SyncUiAction {

    private Collection<HostNicVfsConfig> originalVfsConfigs;
    private Map<Guid, HostNicVfsConfig> updatedNicToVfsConfig;

    private Set<UpdateHostNicVfsConfigParameters> updatedVfsConfigsParams = new HashSet<>();
    private Set<VfsConfigNetworkParameters> addedNetworksParams = new HashSet<>();
    private Set<VfsConfigNetworkParameters> removedNetworksParams = new HashSet<>();
    private Set<VfsConfigLabelParameters> addedLabelsParams = new HashSet<>();
    private Set<VfsConfigLabelParameters> removedLabelsParams = new HashSet<>();

    public VfsConfigAction(Model model,
            Collection<HostNicVfsConfig> originalVfsConfigs,
            Map<Guid, HostNicVfsConfig> updatedNicToVfsConfig) {
        super(model, "VfsConfigAction"); //$NON-NLS-1$
        this.originalVfsConfigs = originalVfsConfigs;
        this.updatedNicToVfsConfig = updatedNicToVfsConfig;
    }

    @Override
    protected void onActionExecute() {
        for (HostNicVfsConfig originalVfsConfig : originalVfsConfigs) {
            HostNicVfsConfig updatedVfsConfig = updatedNicToVfsConfig.get(originalVfsConfig.getNicId());

            initUpdateVfsConfigParams(originalVfsConfig, updatedVfsConfig);

            if (!updatedVfsConfig.isAllNetworksAllowed()) {
                initAddedNetworksParams(originalVfsConfig, updatedVfsConfig);
                initRemovedNetworksParams(originalVfsConfig, updatedVfsConfig);
                initAddedLabelsParams(originalVfsConfig, updatedVfsConfig);
                initRemovedLabelsParams(originalVfsConfig, updatedVfsConfig);
            }
        }

        UiAction updateAction = new UiVdcMultipleAction(ActionType.UpdateHostNicVfsConfig,
                updatedVfsConfigsParams,
                getModel(), true, false);

        updateAction.
                then(new UiVdcMultipleAction(ActionType.AddVfsConfigNetwork,
                        addedNetworksParams,
                        getModel())).
                and(new UiVdcMultipleAction(ActionType.RemoveVfsConfigNetwork,
                        removedNetworksParams,
                        getModel())).
                and(new UiVdcMultipleAction(ActionType.AddVfsConfigLabel,
                        addedLabelsParams,
                        getModel())).
                and(new UiVdcMultipleAction(ActionType.RemoveVfsConfigLabel,
                        removedLabelsParams,
                        getModel())).
                then(getNextAction());

        then(null);

        updateAction.runParallelAction(getActionFlowState());
    }

    private void initUpdateVfsConfigParams(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updatedVfsConfig) {
        if (shouldUpdateVfsConfig(originalVfsConfig, updatedVfsConfig)) {
            UpdateHostNicVfsConfigParameters param =
                    new UpdateHostNicVfsConfigParameters(updatedVfsConfig.getNicId(),
                            updatedVfsConfig.getNumOfVfs(),
                            updatedVfsConfig.isAllNetworksAllowed());
            updatedVfsConfigsParams.add(param);
        }
    }

    private boolean shouldUpdateVfsConfig(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updateVfsConfig) {
        return updateVfsConfig.getNumOfVfs() != originalVfsConfig.getNumOfVfs()
                || updateVfsConfig.isAllNetworksAllowed() != originalVfsConfig.isAllNetworksAllowed();
    }

    private void initAddedNetworksParams(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updateVfsConfig) {
        for (Guid networkId : updateVfsConfig.getNetworks()) {
            if (!originalVfsConfig.getNetworks().contains(networkId)) {
                addedNetworksParams.add(new VfsConfigNetworkParameters(originalVfsConfig.getNicId(), networkId));
            }
        }
    }

    private void initRemovedNetworksParams(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updateVfsConfig) {
        for (Guid networkId : originalVfsConfig.getNetworks()) {
            if (!updateVfsConfig.getNetworks().contains(networkId)) {
                removedNetworksParams.add(new VfsConfigNetworkParameters(originalVfsConfig.getNicId(), networkId));
            }
        }
    }

    private void initAddedLabelsParams(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updateVfsConfig) {
        for (String label : updateVfsConfig.getNetworkLabels()) {
            if (!originalVfsConfig.getNetworkLabels().contains(label)) {
                addedLabelsParams.add(new VfsConfigLabelParameters(originalVfsConfig.getNicId(), label));
            }
        }
    }

    private void initRemovedLabelsParams(HostNicVfsConfig originalVfsConfig, HostNicVfsConfig updateVfsConfig) {
        for (String label : originalVfsConfig.getNetworkLabels()) {
            if (!updateVfsConfig.getNetworkLabels().contains(label)) {
                removedLabelsParams.add(new VfsConfigLabelParameters(originalVfsConfig.getNicId(), label));
            }
        }
    }

}
