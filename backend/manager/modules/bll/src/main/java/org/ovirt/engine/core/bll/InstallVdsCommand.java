package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
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
        return (
            StringUtils.isNotBlank(isoFile) &&
            new File(Config.resolveOVirtISOsRepositoryPath() + File.separator + isoFile).exists()
        );
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
                    RunSleepOnReboot();
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
        } finally {
            if (installer != null) {
                installer.close();
            }
        }
    }

    private void upgradeNode() {
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
            setVdsStatus(VDSStatus.Installing);
            upgrade.execute();

            switch (upgrade.getDeployStatus()) {
                case Failed:
                    throw new VdsInstallException(VDSStatus.InstallFailed, StringUtils.EMPTY);
                case Reboot:
                    setVdsStatus(VDSStatus.Reboot);
                    RunSleepOnReboot();
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
        finally {
            if (upgrade != null) {
                upgrade.close();
            }
        }
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
                RpmVersion isoVersion = new RpmVersion(
                    isoFile,
                    Config.<String> GetValue(ConfigValues.OvirtIsoPrefix),
                    true
                );

                if (!VdsHandler.isIsoVersionCompatibleForUpgrade(ovirtOsVersion, isoVersion)) {
                    retValue = false;
                }
            } catch (RuntimeException e) {
                log.warnFormat(
                    "Failed to parse ISO file version {0} with error {1}",
                    isoFile,
                    ExceptionUtils.getMessage(e)
                );
            }
        }
        return retValue;
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
