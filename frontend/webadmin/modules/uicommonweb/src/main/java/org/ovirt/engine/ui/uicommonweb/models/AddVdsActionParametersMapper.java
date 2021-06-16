package org.ovirt.engine.ui.uicommonweb.models;

import java.util.function.BiFunction;

import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
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
        addVdsActionParams.setRebootHost(model.getRebootHostAfterInstall().getEntity());

        if (model.getProviders().getSelectedItem() != null) {
            addVdsActionParams.getVdsStaticData().setHostProviderId(model.getProviders().getSelectedItem().getId());
        }

        addVdsActionParams.setAffinityGroups(model.getAffinityGroupList().getSelectedItems());
        addVdsActionParams.setAffinityLabels(model.getLabelList().getSelectedItems());

        return addVdsActionParams;
    }
}
