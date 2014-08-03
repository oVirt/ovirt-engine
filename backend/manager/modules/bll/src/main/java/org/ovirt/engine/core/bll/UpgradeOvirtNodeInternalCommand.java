package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

import java.io.IOException;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

@NonTransactiveCommandAttribute
public class UpgradeOvirtNodeInternalCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Log log = LogFactory.getLog(UpgradeOvirtNodeInternalCommand.class);
    protected File _iso = null;
    private VDSStatus vdsInitialStatus;

    private File resolveISO(String iso) {
        File ret = null;

        // do not allow exiting the designated paths
        if (iso != null && iso.indexOf(File.pathSeparatorChar) == -1) {
            for (OVirtNodeInfo.Entry info : OVirtNodeInfo.getInstance().get()) {
                File path = new File(info.path, iso);
                if (path.exists()) {
                    ret = path;
                    break;
                }
            }
        }

        return ret;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private boolean isISOCompatible(
        File iso,
        RpmVersion ovirtHostOsVersion
    ) {
        boolean ret = false;

        log.debugFormat("Check if ISO compatible: {0}", iso);

        for (OVirtNodeInfo.Entry info : OVirtNodeInfo.getInstance().get()) {
            if (info.path.equals(iso.getParentFile())) {
                try {
                    Matcher matcher = info.isoPattern.matcher(
                        iso.getCanonicalFile().getName()
                    );
                    if (matcher.find()) {
                        String rpmLike = matcher.group(1).replaceAll("-", ".");
                        log.debugFormat("ISO version: {0} {1} {2}", iso, rpmLike, ovirtHostOsVersion);
                        RpmVersion isoVersion = new RpmVersion(rpmLike, "", true);
                        if (VdsHandler.isIsoVersionCompatibleForUpgrade(ovirtHostOsVersion, isoVersion)) {
                            log.debugFormat("ISO compatible: {0}", iso);
                            ret = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    log.errorFormat(
                        "Cannot get canonical path to {0} with error {1}",
                        iso.getName(),
                        ExceptionUtils.getMessage(e)
                    );
                }

            }
        }

        return ret;
    }

    public UpgradeOvirtNodeInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsId() == null || getVdsId().equals(Guid.Empty)) {
            return failCanDoAction(VdcBllMessages.VDS_INVALID_SERVER_ID);
        }
        if (getVds() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }
        if (isOvirtReInstallOrUpgrade()) {
            // Block re-install on non-operational Host
            if  (getVds().getStatus() == VDSStatus.NonOperational) {
                return failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL);
            }

            File iso = resolveISO(getParameters().getoVirtIsoFile());
            if (iso == null) {
                return failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
            }

            RpmVersion ovirtHostOsVersion = VdsHandler.getOvirtHostOsVersion(getVds());
            if (!isISOCompatible(iso, ovirtHostOsVersion)) {
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION);
                    addCanDoActionMessageVariable("IsoVersion", ovirtHostOsVersion.getMajor());
                    return false;
            }
            _iso = iso;
        } else {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL);
        }
        return true;
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
        if (getVds() == null) {
            return;
        }

        vdsInitialStatus = getVds().getStatus();
        if (isOvirtReInstallOrUpgrade()) {
            upgradeNode();
        }
    }

    private void upgradeNode() {
        try (
            final OVirtNodeUpgrade upgrade = new OVirtNodeUpgrade(
                getVds(),
                _iso
            )
        ) {
            upgrade.setCorrelationId(getCorrelationId());
            log.infoFormat(
                "Execute upgrade host {0}, {1}",
                getVds().getId(),
                getVds().getName()
            );
            setVdsStatus(VDSStatus.Installing);
            upgrade.execute();

            switch (upgrade.getDeployStatus()) {
                case Failed:
                    throw new VdsInstallException(VDSStatus.InstallFailed, StringUtils.EMPTY);
                case Reboot:
                    setVdsStatus(VDSStatus.Reboot);
                    RunSleepOnReboot(getStatusOnReboot());
                break;
                case Complete:
                    if (!getParameters().getActivateHost() && VDSStatus.Maintenance.equals(vdsInitialStatus)) {
                        setVdsStatus(VDSStatus.Maintenance);
                    } else {
                        setVdsStatus(VDSStatus.Initializing);
                    }
                break;
            }

            log.infoFormat(
                "After upgrade host {0}, {1}: success",
                getVds().getId(),
                getVds().getName()
            );
            setSucceeded(true);
        } catch (VdsInstallException e) {
            handleError(e, e.getStatus());
        } catch (Exception e) {
            handleError(e, VDSStatus.InstallFailed);
        }
    }

    private boolean isOvirtReInstallOrUpgrade() {
        return (
            getParameters().getIsReinstallOrUpgrade() &&
            getVds().getVdsType() == VDSType.oVirtNode
        );
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(
                getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VDS,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED
                )
        );
    }

    private VDSStatus getStatusOnReboot() {
        if (getParameters().getActivateHost()) {
            return VDSStatus.NonResponsive;
        }
        return (VDSStatus.Maintenance.equals(vdsInitialStatus)) ? VDSStatus.Maintenance : VDSStatus.NonResponsive;
    }
}
