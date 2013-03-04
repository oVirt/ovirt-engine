package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@NonTransactiveCommandAttribute
public class InstallVdsCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Log log = LogFactory.getLog(InstallVdsCommand.class);
    private static final String GENERIC_ERROR = "Please refer to enging.log on engine and log files under /tmp on host for further details.";
    protected String _failureMessage = null;

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
            // Block re-install on non-operational Host
            if  (getVds().getStatus() == VDSStatus.NonOperational) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL);
                retValue = false;
            }
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
            addCustomValue("FailedInstallMessage", getErrorMessage(_failureMessage));
            result = AuditLogType.VDS_INSTALL_FAILED;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        if (
            getVds() != null &&
            isOvirtReInstallOrUpgrade()
        ) {
            OVirtNodeUpgrade upgrade = null;
            try {
                T parameters = getParameters();
                upgrade = new OVirtNodeUpgrade(
                    getVds(),
                    parameters.getoVirtIsoFile()
                );
                upgrade.setCorrelationId(getCorrelationId());
                log.infoFormat(
                    "Execute upgrade host {0}, {1}",
                    getVds().getId(),
                    getVds().getName()
                );
                upgrade.execute();
                log.infoFormat(
                    "After upgrade host {0}, {1}: success",
                    getVds().getId(),
                    getVds().getName()
                );
                setSucceeded(true);
                if (getVds().getStatus() == VDSStatus.Reboot) {
                    RunSleepOnReboot();
                }
            }
            catch (Exception e) {
                log.errorFormat(
                    "Host installation failed for host {0}, {1}.",
                    getVds().getId(),
                    getVds().getName(),
                    e
                );
                setSucceeded(false);
                _failureMessage = getErrorMessage(e.getMessage());
                addCustomValue("FailedInstallMessage", _failureMessage);
            }
            finally {
                if (upgrade != null) {
                    upgrade.close();
                }
            }
            return;
        }

        if (getVds() != null) {
            VdsDeploy installer = null;
            try {
                log.infoFormat(
                    "Before Installation host {0}, {1}",
                    getVds().getId(),
                    getVds().getName()
                );

                T parameters = getParameters();
                installer = new VdsDeploy(getVds());
                installer.setCorrelationId(getCorrelationId());
                installer.setReboot(parameters.isRebootAfterInstallation());
                switch (getVds().getVdsType()) {
                case VDS:
                    installer.setUser("root");
                    installer.setPassword(parameters.getRootPassword());
                    installer.setFirewall(parameters.getOverrideFirewall());
                    break;
                case oVirtNode:
                    if (parameters.getOverrideFirewall()) {
                        log.warnFormat(
                            "Installation of Host {0} will ignore Firewall Override option, since it is not supported for Host type {1}",
                            getVds().getName(),
                            getVds().getVdsType().name()
                        );
                    }
                    installer.setUser("root");
                    installer.useDefaultKeyPair();
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(
                            "Not handled VDS type: %1$s",
                            getVds().getVdsType()
                        )
                    );
                }
                installer.execute();
                if (getVds().getStatus() == VDSStatus.Reboot) {
                    RunSleepOnReboot();
                }
                log.infoFormat(
                    "After Installation host {0}, {1}",
                    getVds().getName(),
                    getVds().getVdsType().name()
                );
                setSucceeded(true);
            }
            catch (Exception e) {
                log.errorFormat(
                    "Host installation failed for host {0}, {1}.",
                    getVds().getId(),
                    getVds().getName(),
                    e
                );
                setSucceeded(false);
                _failureMessage = getErrorMessage(e.getMessage());
                addCustomValue("FailedInstallMessage", _failureMessage);
            }
            finally {
                if (installer != null) {
                    installer.close();
                }
            }
            return;
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
        return getParameters().getIsReinstallOrUpgrade() && getVds().getVdsType() == VDSType.oVirtNode;
    }

    protected String getErrorMessage(String msg) {
        return (StringUtils.isEmpty(msg) ? GENERIC_ERROR : msg);
    }

}
