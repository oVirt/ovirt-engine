package org.ovirt.engine.ui.uicommonweb.models;

import java.util.function.BiFunction;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
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
        String sshPublicKeyValue = model.getFetchSshPublicKey().getEntity();
        boolean sshPublicKeySet = sshPublicKeyValue != null && !sshPublicKeyValue.isEmpty();
        host.setSshPublicKey(!sshPublicKeySet ? null : sshPublicKeyValue);
        host.setClusterId(model.getCluster().getSelectedItem().getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save other PM parameters.
        host.setPmEnabled(model.getIsPm().getEntity());
        host.setDisablePowerManagementPolicy(model.getDisableAutomaticPowerManagement().getEntity());
        host.setPmKdumpDetection(model.getPmKdumpDetection().getEntity());

        host.setComment(model.getComment().getEntity());
        boolean consoleAddressSet = model.getConsoleAddressEnabled().getEntity();
        host.setConsoleAddress(!consoleAddressSet ? null : model.getConsoleAddress().getEntity());
        host.setVgpuPlacement(model.getVgpuPlacement().getValue());

        host.setVdsSpmPriority(model.getSpmPriorityValue());
        host.setFenceProxySources(FenceProxySourceTypeHelper.parseFromString(model.getPmProxyPreferences()));

        host.setCurrentKernelCmdline(model.getKernelCmdline().getEntity());
        host.setKernelCmdlineBlacklistNouveau(model.getKernelCmdlineBlacklistNouveau().getEntity());
        host.setKernelCmdlineParsable(model.isKernelCmdlineParsable());
        host.setKernelCmdlineIommu(model.getKernelCmdlineIommu().getEntity());
        host.setKernelCmdlineKvmNested(model.getKernelCmdlineKvmNested().getEntity());
        host.setKernelCmdlineUnsafeInterrupts(model.getKernelCmdlineUnsafeInterrupts().getEntity());
        host.setKernelCmdlinePciRealloc(model.getKernelCmdlinePciRealloc().getEntity());
        host.setKernelCmdlineFips(model.getKernelCmdlineFips().getEntity());
        host.setKernelCmdlineSmtDisabled(model.getKernelCmdlineSmtDisabled().getEntity());

        return host;
    }
}
