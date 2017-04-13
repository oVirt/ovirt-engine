package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collections;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UpgradeModel extends InstallModel {
    private static final String ON_UPGRADE = "OnUpgrade"; //$NON-NLS-1$
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    public UpgradeModel(final VDS host) {
        setVds(host);
    }

    @Override
    public void initialize() {
        setTitle(constants.upgradeHostTitle());
        setHelpTag(HelpTag.upgrade_host);
        setHashName(HelpTag.upgrade_host.name);
        getOVirtISO().setIsAvailable(false);
        getOverrideIpTables().setIsAvailable(false);
        getActivateHostAfterInstall().setEntity(true);
        getHostVersion().setEntity(getVds().getHostOs());
        getHostVersion().setIsAvailable(false);

        AsyncDataProvider.getInstance().getoVirtISOsList(new AsyncQuery<>(isos -> {
            Collections.sort(isos,
                    (rpmV1, rpmV2) -> RpmVersionUtils.compareRpmParts(rpmV2.getRpmName(), rpmV1.getRpmName()));
            getOVirtISO().setItems(isos);
            getOVirtISO().setSelectedItem(Linq.firstOrNull(isos));
            getOVirtISO().setIsAvailable(true);
            getOVirtISO().setIsChangeable(!isos.isEmpty());
            getHostVersion().setIsAvailable(true);

            if (isos.isEmpty()) {
                setMessage(constants.thereAreNoISOversionsVompatibleWithHostCurrentVerMsg());
            }

            if (getVds().getHostOs() == null) {
                setMessage(constants.hostMustBeInstalledBeforeUpgrade());
            }

            addUpgradeCommands(getVds(), isos.isEmpty());
        }),
        getVds().getId());
    }

    private void addUpgradeCommands(VDS host, boolean isOnlyClose) {
        if (!isOnlyClose) {
            UICommand command = UICommand.createDefaultOkUiCommand(ON_UPGRADE, this);
            getCommands().add(0, command);
        }

        getUserName().setEntity(host.getSshUsername());
        getCancelCommand().setTitle(isOnlyClose ? constants.close() : constants.cancel());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (ON_UPGRADE.equals(command.getName())) {
            onUpgrade();
        }
    }

    private void onUpgrade() {
        if (!validate(true)) {
            return;
        }

        UpgradeHostParameters params = new UpgradeHostParameters(getVds().getId());
        params.setoVirtIsoFile(getOVirtISO().getSelectedItem().getRpmName());
        invokeHostUpgrade(params);
    }

    private void invokeHostUpgrade(UpgradeHostParameters params) {
        Frontend.getInstance().runAction(VdcActionType.UpgradeHost, params, result -> {
            VdcReturnValueBase returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                getCancelCommand().execute();
            }
        });
    }
}
