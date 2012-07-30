package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@NonTransactiveCommandAttribute
public class InstallVdsCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Log log = LogFactory.getLog(InstallVdsCommand.class);
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
        } else if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            retValue = false;
        } else if (isOvirtReInstallOrUpgrade()) {
            String isoFile = getParameters().getoVirtIsoFile();
            if (!isIsoFileValid(isoFile)) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
                retValue = false;
            } else {
                RpmVersion ovirtHostOsVersion = VdsHandler.getOvirtHostOsVersion(getVds());
                if (ovirtHostOsVersion != null && !isIsoVersionCompatible(ovirtHostOsVersion, isoFile)) {
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION);
                    addCanDoActionMessage(String.format("$IsoVersion %1$s", ovirtHostOsVersion.getMajor()));
                    retValue = false;
                }
            }
        }
        return retValue;
    }

    private boolean isIsoFileValid(String isoFile) {
        return StringUtils.isNotBlank(isoFile)
                && FileUtil.fileExists(Path.Combine(Config.resolveOVirtISOsRepositoryPath(), isoFile));
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
            T parameters = getParameters();
            if (getVds().getvds_type() == VDSType.VDS) {
                _vdsInstaller =
                        new VdsInstaller(getVds(),
                                parameters.getRootPassword(),
                                parameters.getOverrideFirewall(),
                                parameters.isRebootAfterInstallation());
            } else if (getVds().getvds_type() == VDSType.PowerClient || getVds().getvds_type() == VDSType.oVirtNode) {
                log.infoFormat("Before Installation {0}, Powerclient/oVirtNode case: setting status to installing",
                               Thread.currentThread().getName());
                if (parameters.getOverrideFirewall()) {
                    log.warnFormat("Installation of Host {0} will ignore Firewall Override option, since it is not supported for Host type {1}",
                            getVds().getvds_name(),
                            getVds().getvds_type().name());
                }
                Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                               new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Installing));
                if (isOvirtReInstallOrUpgrade()) {
                    _vdsInstaller = new OVirtUpgrader(getVds(), parameters.getoVirtIsoFile());
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
                        getVds().getId(),
                        getVds().getvds_name(),
                        e);
            }
            setSucceeded(installResult);
            log.infoFormat("After Installation {0}", Thread.currentThread().getName());

            if (getSucceeded()) {
                if (_vdsInstaller.isAddOvirtFlow()) {
                    log.debugFormat("Add manual oVirt flow ended successfully for {0}.", getVds().getvds_name());
                    return;
                }

                switch (getVds().getvds_type()) {
                    case VDS:
                        if (getParameters().isRebootAfterInstallation()) {
                            setHostStatus(VDSStatus.Reboot);
                            RunSleepOnReboot();
                        }
                        else {
                            setHostStatus(VDSStatus.NonResponsive);
                        }
                        break;
                    case oVirtNode:
                        if (getParameters().getIsReinstallOrUpgrade()) {
                            setHostStatus(VDSStatus.Reboot);
                            RunSleepOnReboot();
                        }
                        else {
                            setHostStatus(VDSStatus.NonResponsive);
                        }
                        break;
                    case PowerClient:
                        setHostStatus(VDSStatus.NonResponsive);
                        break;
                }
            }
            else {
                AddCustomValue("FailedInstallMessage", getErrorMessage(_vdsInstaller.getErrorMessage()));
                setHostStatus(VDSStatus.InstallFailed);
            }
        }
    }

    /**
     * Upgrade of image version is allowed only between the same major version of operating system. Both oVirt node OS
     * version and suggested ISO file version are compared to validate version compatibility.
     *
     * @param ovirtOsVersion
     *            the version of the RHEV-H host
     * @param isoFile
     *            the ISO file for upgrade
     * @return true if ISO is compatible with oVirt node OS version or if failed to resolve Host or RHEV-H version
     */
    public boolean isIsoVersionCompatible(RpmVersion ovirtOsVersion, String isoFile) {
        boolean retValue = true;
        if (ovirtOsVersion != null) {
            try {
                RpmVersion isoVersion =
                        new RpmVersion(isoFile, Config.<String> GetValue(ConfigValues.OvirtIsoPrefix), true);

                if (!VdsHandler.isIsoVersionCompatibleForUpgrade(ovirtOsVersion, isoVersion)) {
                    retValue = false;
                }
            } catch (RuntimeException e) {
                log.warnFormat("Failed to parse ISO file version {0} with error {1}",
                        isoFile,
                        ExceptionUtils.getMessage(e));
            }
        }
        return retValue;
    }

    private boolean isOvirtReInstallOrUpgrade() {
        return getParameters().getIsReinstallOrUpgrade() && getVds().getvds_type() == VDSType.oVirtNode;
    }

    private void setHostStatus(VDSStatus stat) {
        Backend.getInstance()
        .getResourceManager()
        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                       new SetVdsStatusVDSCommandParameters(getVdsId(), stat));
    }

    protected String getErrorMessage(String msg)
    {
        return (
                StringHelper.isNullOrEmpty(msg) ?
                        GENERIC_ERROR :
                            msg
        );
    }

}
