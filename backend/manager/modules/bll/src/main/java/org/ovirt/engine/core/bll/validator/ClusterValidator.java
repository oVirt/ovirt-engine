package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class ClusterValidator {

    private final Cluster cluster;
    private final ClusterDao clusterDao;
    private final StoragePoolDao dataCenterDao;
    private StoragePool dataCenter;

    private final CpuFlagsManagerHandler cpuFlagsManagerHandler;

    public ClusterValidator(DbFacade dbFacade, Cluster cluster, CpuFlagsManagerHandler cpuFlagsManagerHandler) {
        this.cluster = cluster;
        this.clusterDao = dbFacade.getClusterDao();
        this.dataCenterDao = dbFacade.getStoragePoolDao();
        this.cpuFlagsManagerHandler = cpuFlagsManagerHandler;
    }

    public ValidationResult nameNotUsed() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE)
                .unless(clusterDao.getByName(cluster.getName(), false).isEmpty());
    }

    /**
     * CPU check is required only if the cluster supports Virt service
     */
    public ValidationResult cpuTypeSupportsVirtService() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND)
                .when(cluster.supportsVirtService() && !cpuExists());
    }

    protected boolean cpuExists() {
        return cpuFlagsManagerHandler.checkIfCpusExist(cluster.getCpuName(), cluster.getCompatibilityVersion());
    }

    public ValidationResult versionSupported() {
        return ValidationResult.failWith(VersionSupport.getUnsupportedVersionMessage())
                .unless(VersionSupport.checkVersionSupported(cluster.getCompatibilityVersion()));
    }

    public ValidationResult dataCenterVersionMismatch() {
        StoragePool dataCenter = getDataCenter();
        return ValidationResult
                .failWith(EngineMessage.CLUSTER_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL)
                .when(dataCenter != null && (cluster.supportsVirtService()
                        ? dataCenter.getCompatibilityVersion().compareTo(cluster.getCompatibilityVersion()) > 0
                        : false));
    }

    public ValidationResult dataCenterExists() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST)
                .when(cluster.getStoragePoolId() != null && getDataCenter() == null);
    }

    public ValidationResult localStoragePoolAttachedToSingleCluster() {
        StoragePool dataCenter = getDataCenter();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE)
                .when(dataCenter != null && dataCenter.isLocal()
                        && !clusterDao.getAllForStoragePool(cluster.getStoragePoolId()).isEmpty());
    }

    public ValidationResult clusterServiceDefined() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED)
                .unless(cluster.supportsGlusterService() || cluster.supportsVirtService());
    }

    public ValidationResult mixedClusterServicesSupported() {
        boolean mixedClusterEnabled = Config.<Boolean> getValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
        return ValidationResult.failWith(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED)
                .when(cluster.supportsGlusterService() && cluster.supportsVirtService() && !mixedClusterEnabled);
    }

    public ValidationResult attestationServerConfigured() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED)
                .when(cluster.supportsTrustedService() && !attestationServerEnabled());
    }

    public ValidationResult migrationSupported(ArchitectureType arch) {
        return ValidationResult.failWith(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED)
                .unless(migrationSupportedForArch(arch));
    }

    protected boolean migrationSupportedForArch(ArchitectureType arch) {
        return FeatureSupported.isMigrationSupported(arch, cluster.getCompatibilityVersion());
    }

    private boolean attestationServerEnabled() {
        String attestationServer = Config.<String> getValue(ConfigValues.AttestationServer);
        return StringUtils.isNotEmpty(attestationServer);
    }

    private StoragePool getDataCenter() {
        if (dataCenter == null && cluster.getStoragePoolId() != null) {
            dataCenter = dataCenterDao.get(cluster.getStoragePoolId());
        }

        return dataCenter;
    }
}
