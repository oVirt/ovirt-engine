package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@NonTransactiveCommandAttribute
public class InstallVdsCommand<T extends InstallVdsParameters> extends VdsCommand<T> {
    protected VdsInstaller _vdsInstaller;
    private static final String GENERIC_ERROR = "Please refer to log files for further details.";

    public InstallVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue=true;
        if (getVdsId() == null || getVdsId().equals(Guid.Empty)) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            retValue = false;
        }
        return retValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType result = null;
        if (getSucceeded()) {
            result = AuditLogType.VDS_INSTALL;
        } else {
            // In case of failure - add to audit log the error as achieved from
            // the host
            AddCustomValue("FailedInstallMessage", getErrorMessage(_vdsInstaller.getErrorMessage()));
            result = AuditLogType.VDS_INSTALL_FAILED;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        if (getVds() != null) {
            if (getVds().getvds_type() == VDSType.VDS) {
                _vdsInstaller =
                        new VdsInstaller(getVds(),
                                getParameters().getRootPassword(),
                                getParameters().getOverrideFirewall());
            } else if (getVds().getvds_type() == VDSType.PowerClient || getVds().getvds_type() == VDSType.oVirtNode) {
                log.infoFormat("Before Installation {0}, Powerclient/oVirtNode case: setting status to installing",
                               Thread.currentThread().getName());
                if (getParameters().getOverrideFirewall()) {
                    log.warnFormat("Installation of Host {0} will ignore Firewall Override option, since it is not supported for Host type {1}",
                            getVds().getvds_name(),
                            getVds().getvds_type().name());
                }
                Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                               new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Installing));
                if (getParameters().getIsReinstallOrUpgrade() && getVds().getvds_type() == VDSType.oVirtNode) {
                    _vdsInstaller = new OVirtInstaller(getVds(), getParameters().getoVirtIsoFile());
                } else {
                    _vdsInstaller = new CBCInstaller(getVds());
                }
            }

            log.infoFormat("Before Installation {0}", Thread.currentThread().getName());
            boolean installResult = false;
            try {
                installResult = _vdsInstaller.Install();
            } catch (Exception e) {
                log.errorFormat("Host installation failed for host {0}, {1}.",
                        getVds().getvds_id(),
                        getVds().getvds_name(),
                        e);
            }
            setSucceeded(installResult);
            log.infoFormat("After Installation {0}", Thread.currentThread().getName());
            if (!getSucceeded()) {
                AddCustomValue("FailedInstallMessage", getErrorMessage(_vdsInstaller.getErrorMessage()));
                Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                               new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.InstallFailed));
            } else {
                if (_vdsInstaller.isAddOvirtFlow()) {
                    log.debugFormat("Add manual oVirt flow ended successfully for {0}.", getVds().getvds_name());
                    return;
                }
                Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                               new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Reboot));
                if (getVds().getvds_type() == VDSType.VDS
                        || (getVds().getvds_type() == VDSType.oVirtNode && getParameters().getIsReinstallOrUpgrade())) {
                    RunSleepOnReboot();
                } else if (getVds().getvds_type() == VDSType.PowerClient || getVds().getvds_type() == VDSType.oVirtNode) {
                    ThreadPoolUtil.execute(new Runnable() {
                        @Override
                        public void run() {
                            CBCSetStatus();
                        }
                    });
                }
            }
        }
    }

    private void CBCSetStatus() {
        Backend.getInstance()
        .getResourceManager()
        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                       new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.NonResponsive));
    }

    protected String getErrorMessage(String msg)
    {
        return (
                StringHelper.isNullOrEmpty(msg) ?
                        GENERIC_ERROR :
                            msg
        );
    }

    private static LogCompat log = LogFactoryCompat.getLog(InstallVdsCommand.class);
}
