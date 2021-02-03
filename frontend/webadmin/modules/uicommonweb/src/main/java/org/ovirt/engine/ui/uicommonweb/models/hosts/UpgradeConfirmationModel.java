package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.CpuUtils;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UpgradeConfirmationModel extends ConfirmationModel {
    private static final String ON_UPGRADE = "OnUpgrade"; //$NON-NLS-1$

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final VDS host;

    private EntityModel<Boolean> reboot;

    public EntityModel<Boolean> getReboot() {
        return reboot;
    }

    public void setReboot(EntityModel<Boolean> reboot) {
        this.reboot = reboot;
    }

    public UpgradeConfirmationModel(final VDS host) {
        this.host = host;
        setReboot(new EntityModel<>());
        getReboot().setEntity(true);
        getReboot().setIsAvailable(true);

        // In case the host is oVirt node, don't allow to disable reboot:
        getReboot().setIsChangeable(!host.isOvirNode());
    }

    @Override
    public void initialize() {
        setTitle(constants.upgradeHostTitle());
        setHelpTag(HelpTag.upgrade_host);
        setHashName(HelpTag.upgrade_host.name);

        if (host.getVmCount() > 0) {
            setMessage(constants.areYouSureYouWantToUpgradeTheFollowingHostWithRunningVmsMsg());
        } else {
            setMessage(constants.areYouSureYouWantToUpgradeTheFollowingHostMsg());
        }

        UICommand upgradeCommand = new UICommand(ON_UPGRADE, this);
        upgradeCommand.setTitle(constants.ok());
        upgradeCommand.setIsDefault(true);
        getCommands().add(upgradeCommand);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (ON_UPGRADE.equals(command.getName())) {
            onUpgrade();
        }
    }

    private void onUpgrade() {
        if (getProgress() != null) {
            return;
        }

        UpgradeHostParameters params = new UpgradeHostParameters(host.getId(), getReboot().getEntity().booleanValue());
        invokeHostUpgrade(params);
    }

    private void invokeHostUpgrade(UpgradeHostParameters params) {
        Frontend.getInstance().runAction(ActionType.UpgradeHost, params, result -> {
            ActionReturnValue returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                getCancelCommand().execute();
            }
        });
    }

    public boolean isClusterCpuInsecureAndAffectedByTsxRemoval() {
        return host.getClusterCompatibilityVersion().less(Version.v4_5)
                && CpuUtils.isCpuInsecureAndAffectedByTsxRemoval(host.getClusterCpuName());
    }
}
