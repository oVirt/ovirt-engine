package org.ovirt.engine.ui.uicommonweb.models;

import java.util.function.BiFunction;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;

public enum VDSMapper implements BiFunction<VDS, HostModel, VDS> {
    INSTANCE;

    @Override
    public VDS apply(VDS host, HostModel model) {
        // Save changes.
        host.setVdsName(model.getName().getEntity());
        host.setHostName(model.getHost().getEntity());
        host.setPort(model.getPort().getEntity());
        host.setSshPort(model.getAuthSshPort().getEntity());
        boolean sshUsernameSet = model.getUserName().getEntity() != null;
        host.setSshUsername(sshUsernameSet ? model.getUserName().getEntity() : null);
        boolean sshFpSet = model.getFetchSshFingerprint().getEntity() != null;
        host.setSshKeyFingerprint(!sshFpSet ? null : model.getFetchSshFingerprint().getEntity());
        host.setClusterId(model.getCluster().getSelectedItem().getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save other PM parameters.
        host.setPmEnabled(model.getIsPm().getEntity());
        host.setDisablePowerManagementPolicy(model.getDisableAutomaticPowerManagement().getEntity());
        host.setPmKdumpDetection(model.getPmKdumpDetection().getEntity());

        return host;
    }
}
