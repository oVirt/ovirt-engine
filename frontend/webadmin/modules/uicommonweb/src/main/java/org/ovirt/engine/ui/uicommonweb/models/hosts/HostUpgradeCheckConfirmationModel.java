package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class HostUpgradeCheckConfirmationModel extends ConfirmationModel {
    private static final String ON_CHECK_UPGRADE = "OnCheckUpgrade"; //$NON-NLS-1$

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final VDS host;

    public HostUpgradeCheckConfirmationModel(final VDS host) {
        this.host = host;
    }

    @Override
    public void initialize() {
        setTitle(constants.upgradeHostTitle());
        setHelpTag(HelpTag.upgrade_host);
        setHashName(HelpTag.upgrade_host.name);
        setMessage(constants.areYouSureYouWantToCheckTheFollowingHostForUpgradesMsg());

        UICommand upgradeCommand = new UICommand(ON_CHECK_UPGRADE, this);
        upgradeCommand.setTitle(constants.ok());
        upgradeCommand.setIsDefault(true);
        getCommands().add(upgradeCommand);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (ON_CHECK_UPGRADE.equals(command.getName())) {
            onUpgrade();
        }
    }

    private void onUpgrade() {
        if (getProgress() != null) {
            return;
        }

        invokeCheckForHostUpgrade(new VdsActionParameters(host.getId()));
    }

    private void invokeCheckForHostUpgrade(VdsActionParameters params) {
        Frontend.getInstance().runAction(ActionType.HostUpgradeCheck, params, result -> {
            ActionReturnValue returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                getCancelCommand().execute();
            }
        });
    }
}
