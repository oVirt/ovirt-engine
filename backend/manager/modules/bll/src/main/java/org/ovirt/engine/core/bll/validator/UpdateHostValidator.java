package org.ovirt.engine.core.bll.validator;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.Injector;

public class UpdateHostValidator extends HostValidator {

    private final VDS oldHost;
    private final boolean installHost;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private VdsHandler vdsHandler;

    private Provider<?> provider;

    public static UpdateHostValidator createInstance(VDS oldHost, VDS updatedHost, boolean installHost) {
        return Injector.injectMembers(new UpdateHostValidator(oldHost, updatedHost, installHost));
    }

    public UpdateHostValidator(VDS oldHost, VDS updatedHost, boolean installHost) {
        super(updatedHost);
        this.oldHost = oldHost;
        this.installHost = installHost;
    }

    @Override
    public ValidationResult hostExists() {
        return ValidationResult.failWith(EngineMessage.VDS_INVALID_SERVER_ID)
                .when(oldHost == null || getHost() == null);
    }

    public ValidationResult hostStatusValid() {
        return ValidationResult.failWith(EngineMessage.VDS_STATUS_NOT_VALID_FOR_UPDATE).when(!isUpdateValid());
    }

    protected boolean isUpdateValid() {
        return vdsHandler.isUpdateValid(getHost().getStaticData(), oldHost.getStaticData(), oldHost.getStatus());
    }

    public ValidationResult updateHostAddressAllowed() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_HOSTNAME_CANNOT_CHANGE)
                .when(oldHost.getStatus() != VDSStatus.InstallFailed
                        && !oldHost.getHostName().equals(getHost().getHostName()));
    }

    @Override
    public ValidationResult nameNotUsed() {
        if (StringUtils.equalsIgnoreCase(oldHost.getName(), getHost().getName())) {
            return ValidationResult.VALID;
        }

        return super.nameNotUsed();
    }

    @Override
    public ValidationResult hostNameNotUsed() {
        if (StringUtils.equalsIgnoreCase(oldHost.getHostName(), getHost().getHostName())) {
            return ValidationResult.VALID;
        }

        return super.hostNameNotUsed();
    }

    public ValidationResult statusSupportedForHostInstallation() {
        return ValidationResult.failWith(EngineMessage.VDS_CANNOT_INSTALL_STATUS_ILLEGAL)
                .when(installHost
                        && oldHost.getStatus() != VDSStatus.Maintenance
                        && oldHost.getStatus() != VDSStatus.NonOperational
                        && oldHost.getStatus() != VDSStatus.InstallFailed
                        && oldHost.getStatus() != VDSStatus.InstallingOS);
    }

    public ValidationResult passwordProvidedForHostInstallation(AuthenticationMethod method, String password) {
        return ValidationResult.failWith(EngineMessage.VDS_CANNOT_INSTALL_EMPTY_PASSWORD)
                .when(installHost
                        && method == AuthenticationMethod.Password
                        && StringUtils.isEmpty(password)
                        && getHost().getVdsType() == VDSType.VDS);
    }

    public ValidationResult updatePortAllowed() {
        return ValidationResult.failWith(EngineMessage.VDS_PORT_CHANGE_REQUIRE_INSTALL)
                .unless(installHost || oldHost.getPort() == getHost().getPort());
    }

    /**
     * Forbids updating group id - this must be done through {@code ChangeVDSClusterCommand}. This is due to permission
     * check that must be done both on the VDS and on the Cluster
     */
    public ValidationResult clusterNotChanged() {
        return ValidationResult.failWith(EngineMessage.VDS_CANNOT_UPDATE_CLUSTER)
                .unless(oldHost.getClusterId().equals(getHost().getClusterId()));
    }

    public ValidationResult hostProviderExists() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(getHost().getHostProviderId() != null && getProvider() == null);
    }

    public ValidationResult hostProviderTypeMatches() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_PROVIDER_TYPE_MISMATCH)
                .when(getProvider() != null && getProvider().getType() != ProviderType.FOREMAN);
    }

    private Provider<?> getProvider() {
        if (getHost().getHostProviderId() != null && provider == null) {
            provider = providerDao.get(getHost().getHostProviderId());
        }

        return provider;
    }
}
