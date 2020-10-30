package org.ovirt.engine.ui.uicommonweb.models;

import java.util.function.BiFunction;

import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;

public enum AddVdsActionParametersMapper implements BiFunction<VDS, HostModel, AddVdsActionParameters> {

    INSTANCE;

    @Override
    public AddVdsActionParameters apply(VDS host, HostModel model) {
        AddVdsActionParameters addVdsActionParams = new AddVdsActionParameters();
        addVdsActionParams.setVdsId(host.getId());
        addVdsActionParams.setvds(host);
        if (model.getUserPassword().getEntity() != null) {
            addVdsActionParams.setPassword(model.getUserPassword().getEntity());
        }
        addVdsActionParams.setAuthMethod(model.getAuthenticationMethod());
        addVdsActionParams.setOverrideFirewall(model.getOverrideIpTables().getEntity());
        addVdsActionParams.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
        addVdsActionParams.setHostedEngineDeployConfiguration(
                new HostedEngineDeployConfiguration(model.getHostedEngineHostModel().getSelectedItem()));
        addVdsActionParams.setActivateHost(model.getActivateHostAfterInstall().getEntity());

        if (model.getProviders().getSelectedItem() != null) {
            addVdsActionParams.getVdsStaticData().setHostProviderId(model.getProviders().getSelectedItem().getId());
        }
        if (Boolean.TRUE.equals(model.getIsDiscoveredHosts().getEntity())) {
            Provider<?> provider = model.getProviders().getSelectedItem();
            ExternalHostGroup hostGroup = (ExternalHostGroup) model.getExternalHostGroups().getSelectedItem();
            ExternalComputeResource computeResource = (ExternalComputeResource) model.getExternalComputeResource().getSelectedItem();
            ExternalDiscoveredHost discoveredHost = (ExternalDiscoveredHost) model.getExternalDiscoveredHosts().getSelectedItem();
            addVdsActionParams.initVdsActionParametersForProvision(
                    provider.getId(),
                    hostGroup,
                    computeResource,
                    discoveredHost.getMac(),
                    discoveredHost.getName(),
                    discoveredHost.getIp());
        }

        addVdsActionParams.setAffinityGroups(model.getAffinityGroupList().getSelectedItems());
        addVdsActionParams.setAffinityLabels(model.getLabelList().getSelectedItems());

        return addVdsActionParams;
    }
}
