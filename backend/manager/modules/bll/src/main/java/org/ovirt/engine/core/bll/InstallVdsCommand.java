package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class InstallVdsCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Log log = LogFactory.getLog(InstallVdsCommand.class);
    private static final String GENERIC_ERROR = "Please refer to engine.log and log files under /var/log/ovirt-engine/host-deploy/ on the engine for further details.";
    protected String _failureMessage = null;
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

    private boolean isISOCompatible(
        File iso,
        RpmVersion ovirtHostOsVersion
    ) {
        boolean ret = false;

        log.debugFormat("Check if ISO compatible: {0}", iso);

        for (OVirtNodeInfo.Entry info : OVirtNodeInfo.getInstance().get()) {
            if (info.path.equals(iso.getParentFile())) {
                Matcher matcher = info.isoPattern.matcher(iso.getName());
                if (matcher.find()) {
                    String rpmLike = matcher.group(1).replaceAll("-", ".");
                    log.debugFormat("ISO version: {0} {1} {3}", iso, rpmLike, ovirtHostOsVersion);
                    RpmVersion isoVersion = new RpmVersion(rpmLike, "", true);
                    if (VdsHandler.isIsoVersionCompatibleForUpgrade(ovirtHostOsVersion, isoVersion)) {
                        log.debugFormat("ISO compatible: {0}", iso);
                        ret = true;
                        break;
                    }
                }
            }
        }

        return ret;
    }

    public InstallVdsCommand(T parameters) {
        super(parameters);
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
                    addCanDoActionMessage(String.format("$IsoVersion %1$s", ovirtHostOsVersion.getMajor()));
                    return false;
            }
            _iso = iso;
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
        } else {
            installHost();
        }
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

    private void installHost() {
        try (final VdsDeploy installer = new VdsDeploy(getVds())) {
            log.infoFormat(
                "Before Installation host {0}, {1}",
                getVds().getId(),
                getVds().getName()
            );

            T parameters = getParameters();
            installer.setCorrelationId(getCorrelationId());
            boolean configureNetworkUsingHostDeploy = !FeatureSupported.setupManagementNetwork(
                getVds().getVdsGroupCompatibilityVersion()
            );
            installer.setReboot(
                parameters.isRebootAfterInstallation() &&
                configureNetworkUsingHostDeploy
            );

            if (configureNetworkUsingHostDeploy) {
                installer.setManagementNetwork(NetworkUtils.getEngineNetwork());
            }

            if (parameters.getProviderId() != null) {
                Provider<?> provider = getDbFacade().getProviderDao().get(parameters.getProviderId());
                if (provider.getType() == ProviderType.OPENSTACK_NETWORK) {
                    OpenstackNetworkProviderProperties agentProperties =
                            (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                    if (parameters.getNetworkMappings() != null) {
                        agentProperties.getAgentConfiguration().setNetworkMappings(
                            parameters.getNetworkMappings()
                        );
                    }
                    installer.setOpenStackAgentProperties(agentProperties);
                }
            }

            switch (getVds().getVdsType()) {
                case VDS:
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
                break;
                default:
                    throw new IllegalArgumentException(
                        String.format(
                            "Not handled VDS type: %1$s",
                            getVds().getVdsType()
                        )
                    );
            }

            switch (getParameters().getAuthMethod()) {
                case Password:
                    installer.setPassword(parameters.getPassword());
                break;
                case PublicKey:
                    installer.useDefaultKeyPair();
                break;
                default:
                    throw new Exception("Invalid authentication method value was sent to InstallVdsCommand");
            }

            setVdsStatus(VDSStatus.Installing);
            installer.execute();

            switch (installer.getDeployStatus()) {
                case Failed:
                    throw new VdsInstallException(VDSStatus.InstallFailed, StringUtils.EMPTY);
                case Incomplete:
                    throw new VdsInstallException(VDSStatus.InstallFailed, "Partial installation");
                case Reboot:
                    setVdsStatus(VDSStatus.Reboot);
                    RunSleepOnReboot(getStatusOnReboot());
                break;
                case Complete:
                    if (!configureNetworkUsingHostDeploy) {
                        configureManagementNetwork();
                    }
                    setVdsStatus(VDSStatus.Initializing);
                break;
            }

            log.infoFormat(
                "After Installation host {0}, {1}",
                getVds().getName(),
                getVds().getVdsType().name()
            );
            setSucceeded(true);
        } catch (VdsInstallException e) {
            handleError(e, e.getStatus());
        } catch (Exception e) {
            handleError(e, VDSStatus.InstallFailed);
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
                    setVdsStatus(VDSStatus.Initializing);
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

    private VDSStatus getStatusOnReboot() {
        return (VDSStatus.Maintenance.equals(vdsInitialStatus)) ? VDSStatus.Maintenance : VDSStatus.NonResponsive;
    }

    private void handleError(Exception e, VDSStatus status) {
        log.errorFormat(
            "Host installation failed for host {0}, {1}.",
            getVds().getId(),
            getVds().getName(),
            e
        );
        setVdsStatus(status);
        setSucceeded(false);
        _failureMessage = e.getMessage();
    }

    private void configureManagementNetwork() {
        final NetworkConfigurator networkConfigurator = new NetworkConfigurator(getVds());
        if (!networkConfigurator.awaitVdsmResponse()) {
            throw new VdsInstallException(
                VDSStatus.NonResponsive,
                "Network error during communication with the host"
            );
        }

        try {
            networkConfigurator.refreshNetworkConfiguration();
            networkConfigurator.createManagementNetworkIfRequired();
        } catch (VDSNetworkException e) {
            throw new VdsInstallException(
                VDSStatus.NonResponsive,
                "Network error during communication with the host",
                e
            );
        } catch (Exception e) {
            throw new VdsInstallException(
                VDSStatus.NonOperational,
                "Failed to configure management network on the host",
                e
            );
        }
    }

    /**
     * Set vds object status.
     *
     * @param status
     *            new status.
     */
    private void setVdsStatus(VDSStatus status) {
        runVdsCommand(
            VDSCommandType.SetVdsStatus,
            new SetVdsStatusVDSCommandParameters(getVdsId(), status)
        );
    }

    private boolean isOvirtReInstallOrUpgrade() {
        return (
            getParameters().getIsReinstallOrUpgrade() &&
            getVds().getVdsType() == VDSType.oVirtNode
        );
    }

    protected String getErrorMessage(String msg) {
        return StringUtils.isEmpty(msg) ? GENERIC_ERROR : msg;
    }

    @SuppressWarnings("serial")
    private static class VdsInstallException extends RuntimeException {
        private VDSStatus status;

        VdsInstallException(VDSStatus status, String message) {
            super(message);
            this.status = status;
        }

        VdsInstallException(VDSStatus status, String message, Exception cause) {
            super(message, cause);
            this.status = status;
        }

        public VDSStatus getStatus() {
            return status;
        }
    }
}
