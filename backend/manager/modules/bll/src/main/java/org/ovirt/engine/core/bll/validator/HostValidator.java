package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

public class HostValidator {

    private VdsDao hostDao;
    private StoragePoolDao storagePoolDao;
    private VdsStaticDao hostStaticDao;
    private VDS host;
    private HostedEngineHelper hostedEngineHelper;
    private ClusterDao clusterDao;

    private ValidationResult validateStatus(VDSStatus hostStatus) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                ReplacementUtils.createSetVariableString("hostStatus", hostStatus.name()))
                .unless(hostStatus == host.getStatus());
    }

    public HostValidator(DbFacade dbFacade, VDS host, HostedEngineHelper hostedEngineHelper) {
        this.hostDao = dbFacade.getVdsDao();
        this.storagePoolDao = dbFacade.getStoragePoolDao();
        this.hostStaticDao = dbFacade.getVdsStaticDao();
        this.host = host;
        this.hostedEngineHelper = hostedEngineHelper;
        this.clusterDao = dbFacade.getClusterDao();
    }

    public HostValidator(VDS host) {
        this.host = host;
    }

    public ValidationResult hostExists() {
        return ValidationResult.failWith(EngineMessage.VDS_INVALID_SERVER_ID).when(getHost() == null);
    }

    public ValidationResult nameNotEmpty() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY)
                .when(StringUtils.isEmpty(host.getName()));
    }

    public ValidationResult nameLengthIsLegal() {
        int maxHostNameLength = Config.<Integer> getValue(ConfigValues.MaxVdsNameLength);
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG)
                .when(host.getName().length() > maxHostNameLength);
    }

    public ValidationResult hostNameIsValid() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME)
                .unless(ValidationUtils.validHostname(host.getHostName()));
    }

    public ValidationResult nameNotUsed() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(hostDao.getByName(host.getName()) != null);
    }

    public ValidationResult hostNameNotUsed() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST)
                .unless(hostDao.getAllForHostname(host.getHostName()).isEmpty());
    }

    public ValidationResult portIsValid() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT)
                .unless(ValidationUtils.validatePort(host.getSshPort()));
    }

    public ValidationResult sshUserNameNotEmpty() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME)
                .when(StringUtils.isBlank(host.getSshUsername()));
    }

    public ValidationResult validateSingleHostAttachedToLocalStorage() {
        StoragePool storagePool = storagePoolDao.getForCluster(host.getClusterId());
        if (storagePool == null || !storagePool.isLocal()) {
            return ValidationResult.VALID;
        }

        return ValidationResult.failWith(EngineMessage.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE)
                .unless(hostStaticDao.getAllForCluster(host.getClusterId()).isEmpty());
    }

    public ValidationResult securityKeysExists() {
        return ValidationResult.failWith(EngineMessage.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND)
                .when(Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication) && !haveSecurityKey());
    }

    public ValidationResult provisioningComputeResourceValid(boolean provisioned,
            ExternalComputeResource computeResource) {
        return ValidationResult.failWith(EngineMessage.VDS_PROVIDER_PROVISION_MISSING_COMPUTERESOURCE)
                .when(provisioned && computeResource == null);
    }

    public ValidationResult provisioningHostGroupValid(boolean provisioned, ExternalHostGroup hostGroup) {
        return ValidationResult.failWith(EngineMessage.VDS_PROVIDER_PROVISION_MISSING_HOSTGROUP)
                .when(provisioned && hostGroup == null);
    }

    public ValidationResult protocolIsNotXmlrpc() {
        return ValidationResult.failWith(EngineMessage.NOT_SUPPORTED_PROTOCOL_FOR_CLUSTER_VERSION)
                .when(VdsProtocol.XML == host.getProtocol());
    }

    protected boolean haveSecurityKey() {
        return EngineEncryptionUtils.haveKey();
    }

    /**
     * We block vds installations if it's not a RHEV-H and password is empty. Note that this may override local host SSH
     * policy. See BZ#688718.
     */
    public ValidationResult passwordNotEmpty(boolean addPending, AuthenticationMethod authMethod, String password) {
        return ValidationResult.failWith(EngineMessage.VDS_CANNOT_INSTALL_EMPTY_PASSWORD)
                .when(!addPending && authMethod == AuthenticationMethod.Password && StringUtils.isEmpty(password));
    }

    public ValidationResult validateStatusForActivation() {
        ValidationResult existsValidation = hostExists();
        if (!existsValidation.isValid()) {
            return existsValidation;
        }
        if (VDSStatus.Up == host.getStatus()) {
            return new ValidationResult(EngineMessage.VDS_ALREADY_UP);
        }
        if (VDSStatus.NonResponsive == host.getStatus()) {
            return new ValidationResult(EngineMessage.VDS_NON_RESPONSIVE);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateUniqueId() {
        return ValidationResult.failWith(EngineMessage.VDS_NO_UUID)
                .when(StringUtils.isBlank(host.getUniqueId()) && Config.<Boolean> getValue(ConfigValues.InstallVds));
    }

    public ValidationResult isUp() {
        return validateStatus(VDSStatus.Up);
    }

    protected VDS getHost() {
        return host;
    }

    public ValidationResult validateStatusForEnrollCertificate() {
        return ValidationResult.failWith(EngineMessage.CANNOT_ENROLL_CERTIFICATE_HOST_STATUS_ILLEGAL)
                .unless(host.getStatus() == VDSStatus.Maintenance || host.getStatus() == VDSStatus.InstallFailed);
    }

    public ValidationResult supportsDeployingHostedEngine(HostedEngineDeployConfiguration heConfig) {
        if (heConfig == null) {
            return ValidationResult.VALID;
        }
        final Cluster cluster = clusterDao.get(host.getClusterId());
        if (cluster.getCompatibilityVersion().less(Version.v4_0)
                && heConfig.getDeployAction() != HostedEngineDeployConfiguration.Action.NONE) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DEPLOYMENT_UNSUPPORTED,
                    "$deployAction " + heConfig.getDeployAction().name().toLowerCase(),
                    "$clusterLevel " + cluster.getCompatibilityVersion());
        }
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE)
                .when(heConfig.getDeployAction() == HostedEngineDeployConfiguration.Action.DEPLOY
                        && !hostedEngineHelper.isVmManaged());
    }
}
