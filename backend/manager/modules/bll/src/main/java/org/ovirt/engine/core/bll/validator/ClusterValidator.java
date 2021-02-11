package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;

public class ClusterValidator {

    public static final Set<VmRngDevice.Source> ALLOWED_ADDITIONAL_RNG_SOURCES =
            Collections.singleton(VmRngDevice.Source.HWRNG);

    private final Cluster cluster;
    private final ClusterDao clusterDao;
    private final StoragePoolDao dataCenterDao;
    private StoragePool dataCenter;
    private StoragePool dataCenterOfNewCluster;
    private GlusterVolumeDao glusterVolumeDao;
    private ClusterFeatureDao clusterFeatureDao;
    private SupportedHostFeatureDao hostFeatureDao;
    private NetworkDao networkDao;

    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    private LabelDao labelDao;
    private Cluster newCluster;

    private int compareCompatibilityVersions;

    private final Supplier<List<VDS>> allHostsForCluster;
    private final Supplier<List<VM>> allVmsForCluster;

    public ClusterValidator(
            ClusterDao clusterDao,
            StoragePoolDao dataCenterDao,
            Cluster cluster,
            CpuFlagsManagerHandler cpuFlagsManagerHandler) {
        this.cluster = cluster;
        this.clusterDao = clusterDao;
        this.dataCenterDao = dataCenterDao;
        this.cpuFlagsManagerHandler = cpuFlagsManagerHandler;

        allHostsForCluster = () -> null;
        allVmsForCluster = () -> null;
    }

    public ClusterValidator(ClusterDao clusterDao,
                            StoragePoolDao dataCenterDao,
                            Cluster cluster,
                            CpuFlagsManagerHandler cpuFlagsManagerHandler,
                            Cluster newCluster,
                            VdsDao vdsDao,
                            VmDao vmDao,
                            GlusterVolumeDao glusterVolumeDao,
                            ClusterFeatureDao clusterFeatureDao,
                            SupportedHostFeatureDao hostFeatureDao,
                            LabelDao labelDao,
                            NetworkDao networkDao) {
        this.cluster = cluster;
        this.clusterDao = clusterDao;
        this.dataCenterDao = dataCenterDao;
        this.cpuFlagsManagerHandler = cpuFlagsManagerHandler;
        this.newCluster = newCluster;
        this.glusterVolumeDao = glusterVolumeDao;
        this.clusterFeatureDao = clusterFeatureDao;
        this.hostFeatureDao = hostFeatureDao;
        this.labelDao = labelDao;
        this.networkDao = networkDao;

        allHostsForCluster = new MemoizingSupplier<>(() -> vdsDao.getAllForCluster(cluster.getId()));
        allVmsForCluster = new MemoizingSupplier<>(() -> vmDao.getAllForCluster(cluster.getId()));
    }

    Cluster getNewCluster() {
        return newCluster;
    }

    public ValidationResult nameNotUsed() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE)
                .unless(clusterDao.getByName(cluster.getName(), false).isEmpty());
    }

    /**
     * if the name was changed then make sure the new name is unique
     */
    public ValidationResult newNameUnique() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE)
                .when(!Objects.equals(cluster.getName(), newCluster.getName())
                        && !clusterDao.getByName(newCluster.getName(), false).isEmpty());
    }

    /**
     * CPU check is required only if the cluster supports Virt service
     */
    public ValidationResult cpuTypeSupportsVirtService() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND)
                .when(cluster.supportsVirtService() && !cpuExists());
    }

    protected boolean cpuExists() {
        return cluster.getCpuName() == null || "".equals(cluster.getCpuName()) ||
                cpuFlagsManagerHandler.checkIfCpusExist(cluster.getCpuName(), cluster.getCompatibilityVersion());
    }

    public ValidationResult versionSupported() {
        return ValidationResult.failWith(VersionSupport.getUnsupportedVersionMessage())
                .unless(VersionSupport.checkVersionSupported(cluster.getCompatibilityVersion()));
    }

    public ValidationResult newClusterVersionSupported() {
        return ValidationResult.failWith(VersionSupport.getUnsupportedVersionMessage())
                .unless(VersionSupport.checkVersionSupported(newCluster.getCompatibilityVersion()));
    }

    public ValidationResult atLeastOneHostSupportingClusterVersion() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_VERSION_WHEN_NO_HOST_SUPPORTS_THE_VERSION)
                .unless(allHostsForCluster.get().isEmpty()
                        || allHostsForCluster.get()
                                .stream()
                                .anyMatch(h -> h.getSupportedClusterVersionsSet().contains(
                                        newCluster.getCompatibilityVersion())));
    }

    public ValidationResult dataCenterVersionMismatch() {
        StoragePool dataCenter = getDataCenter();
        return ValidationResult
                .failWith(EngineMessage.CLUSTER_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL)
                .when(dataCenter != null && cluster.supportsVirtService() &&
                        dataCenter.getCompatibilityVersion().compareTo(cluster.getCompatibilityVersion()) > 0);
    }

    public ValidationResult localStoragePoolAttachedToSingleCluster() {
        StoragePool dataCenter = getDataCenter();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE)
                .when(dataCenter != null && dataCenter.isLocal()
                        && !clusterDao.getAllForStoragePool(cluster.getStoragePoolId()).isEmpty());
    }

    /**
     * we allow only one cluster in localfs data center
     */
    public ValidationResult addMoreThanOneHost() {
        StoragePool dataCenter = getDataCenterOfNewCluster();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE)
                .when(newCluster.getStoragePoolId() != null && cluster.getStoragePoolId() == null
                        && dataCenter.isLocal()
                        && !clusterDao.getAllForStoragePool(newCluster.getStoragePoolId()).isEmpty());
    }

    public ValidationResult defaultClusterOnLocalfs() {
        if (newCluster.getStoragePoolId() != null) {
            StoragePool dataCenter = dataCenterDao.get(newCluster.getStoragePoolId());
            if (cluster.getStoragePoolId() == null && dataCenter.isLocal()) {
                return ValidationResult.failWith(EngineMessage.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS)
                        .when(Config.getValue(ConfigValues.AutoRegistrationDefaultClusterID)
                                .equals(newCluster.getId()));
            }
        }
        return ValidationResult.VALID;
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

    public ValidationResult mixedClusterServicesSupportedForNewCluster() {
        boolean mixedClusterEnabled = isAllowClusterWithVirtGluster();
        return ValidationResult.failWith(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED)
                .when(newCluster.supportsGlusterService() && newCluster.supportsVirtService()
                        && !mixedClusterEnabled);
    }

    public ValidationResult attestationServerConfigured() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED)
                .when(cluster.supportsTrustedService() && !attestationServerEnabled());
    }

    public ValidationResult migrationSupported(ArchitectureType arch) {
        return ValidationResult.failWith(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED)
                .unless(migrationSupportedForArch(arch));
    }

    public ValidationResult rngSourcesAllowed() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_RANDOM_RNG_SOURCE_CANT_BE_ADDED_TO_CLUSTER_ADDITIONAL_RNG_SOURCES)
                .unless(ALLOWED_ADDITIONAL_RNG_SOURCES.containsAll(cluster.getAdditionalRngSources()));
    }

    public ValidationResult memoryOptimizationConfiguration() {
        //if maxVdsMemoryOverCommit is set to a value <=0, it is later translated to maxVdsMemoryOverCommit = 100
        //so we need to allow it here
        return ValidationResult.failWith(EngineMessage.CLUSTER_TO_ALLOW_MEMORY_OPTIMIZATION_YOU_MUST_ALLOW_KSM_OR_BALLOONING)
                .when(cluster.getMaxVdsMemoryOverCommit() > 100
                        && !(cluster.isEnableKsm() || cluster.isEnableBallooning()));
    }

    public ValidationResult oldClusterIsValid() {
        return ValidationResult.failWith(EngineMessage.VDS_CLUSTER_IS_NOT_VALID)
                .when(cluster == null);
    }

    /**
     * decreasing of compatibility version is only allowed when no hosts exist
     */
    public ValidationResult decreaseClusterWithHosts() {
        compareCompatibilityVersions = newCluster.getCompatibilityVersion().compareTo(cluster.getCompatibilityVersion());
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_CLUSTER_WITH_HOSTS_COMPATIBILITY_VERSION)
                .when(compareCompatibilityVersions < 0
                        && !allHostsForCluster.get().isEmpty());
    }

    /**
     * decreasing of compatibility version is only allowed when not beneath the DC version
     */
    public ValidationResult decreaseClusterBeneathDc(ClusterValidator oldClusterValidator) {
        compareCompatibilityVersions = newCluster.getCompatibilityVersion().compareTo(cluster.getCompatibilityVersion());
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC)
                .when(compareCompatibilityVersions < 0
                        && cluster.getStoragePoolId() != null
                        && !oldClusterValidator.dataCenterVersionMismatch().isValid());
    }


    public ValidationResult decreaseClusterWithPortIsolation() {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_PORT_ISOLATION_UNSUPPORTED_CLUSTER_LEVEL)
                .when(networkDao.getAllForCluster(cluster.getId()).stream().anyMatch(Network::isPortIsolation)
                        && !FeatureSupported.isPortIsolationSupported(getNewCluster().getCompatibilityVersion()));
    }


    private boolean areAllVdssInMaintenance(List<VDS> vdss) {
        return vdss.stream().allMatch(vds -> vds.getStatus() == VDSStatus.Maintenance);
    }

    /**
     * validate the cpu only if the cluster supports Virt
     */
    public ValidationResult cpuNotFound(boolean cpusExist) {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ILLEGAL)
                .when(newCluster.supportsVirtService()
                        && (!"".equals(cluster.getCpuName()) || !"".equals(newCluster.getCpuName()))
                        && !cpusExist);
    }

    /**
     *
     * if cpu changed from intel to amd (or backwards) and there are vds in this cluster, cannot update
     */
    public ValidationResult updateCpuIllegal(boolean cpusExist, boolean cpusSameManufacture) {
        boolean allVdssInMaintenance = areAllVdssInMaintenance(allHostsForCluster.get());
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ILLEGAL)
                .when(newCluster.supportsVirtService()
                        && (!"".equals(cluster.getCpuName()) || !"".equals(newCluster.getCpuName()))
                        && cpusExist && !StringUtils.isEmpty(cluster.getCpuName())
                        && !cpusSameManufacture
                        && !allVdssInMaintenance);
    }

    public ValidationResult updateFipsIsLegal() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_FIPS_VDS_MAINTENANCE)
                .when(cluster.getFipsMode() != newCluster.getFipsMode()
                        && !areAllVdssInMaintenance(allHostsForCluster.get()));
    }

    /**
     * cannot change the processor architecture while there are attached hosts or VMs to the cluster
     */
    public ValidationResult architectureIsLegal(boolean isArchitectureUpdatable) {
        boolean hasVmOrHost = !allVmsForCluster.get().isEmpty() || !allHostsForCluster.get().isEmpty();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL)
                .when(newCluster.supportsVirtService() && !isArchitectureUpdatable && hasVmOrHost);
    }

    public ValidationResult cpuUpdatable() {
        boolean hasVmOrHost = !allVmsForCluster.get().isEmpty() || !allHostsForCluster.get().isEmpty();
        boolean sameCpuNames = Objects.equals(cluster.getCpuName(), newCluster.getCpuName());
        boolean isCpuUpdatable = cpuFlagsManagerHandler.isCpuUpdatable(cluster.getCpuName(), cluster.getCompatibilityVersion());
        boolean isOldCPUEmpty = StringUtils.isEmpty(cluster.getCpuName());
        return ValidationResult.failWith(EngineMessage.CLUSTER_CPU_IS_NOT_UPDATABLE)
                .when(!isOldCPUEmpty && !sameCpuNames && !isCpuUpdatable && hasVmOrHost);
    }

    public ValidationResult canAutoDetectCpu() {
        boolean oldCpuEmpty = StringUtils.isEmpty(cluster.getCpuName());
        boolean newCpuEmpty = StringUtils.isEmpty(newCluster.getCpuName());
        boolean hasVmOrHost = !allVmsForCluster.get().isEmpty() || !allHostsForCluster.get().isEmpty();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_SET_CPU_AUTODETECTION)
                .when(!oldCpuEmpty && newCpuEmpty && hasVmOrHost);
    }

    /**
     * can't change cluster version when a VM is in preview
     */
    public ValidationResult vmInPrev() {
        List<String> vmInPreviewNames = allVmsForCluster.get().stream()
                .filter(VM::isPreviewSnapshot)
                .map(VM::getName)
                .collect(Collectors.toList());
        return ValidationResult.failWith(EngineMessage.CLUSTER_VERSION_CHANGE_VM_PREVIEW, vmInPreviewNames)
                .when(!cluster.getCompatibilityVersion().equals(newCluster.getCompatibilityVersion())
                        && !vmInPreviewNames.isEmpty());
    }

    protected List<VDS> upVdss() {
        return allHostsForCluster.get().stream()
                .filter(v -> v.getStatus() == VDSStatus.Up)
                .collect(Collectors.toList());
    }

    public ValidationResult vdsUp() {
        List<VDS> upVdss = upVdss();
        boolean isAddedToStoragePool = cluster.getStoragePoolId() == null
                && newCluster.getStoragePoolId() != null;
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_VDS_UP)
                .when(!upVdss.isEmpty() && isAddedToStoragePool);
    }

    public ValidationResult hostsDown(boolean isForceResetEmulatedMachine) {
        List<VDS> upVdss = upVdss();
        return ValidationResult.failWith(EngineMessage.CLUSTER_HOSTS_MUST_BE_DOWN)
                .when(!upVdss.isEmpty() && isForceResetEmulatedMachine);
    }

    /**
     * Lets not modify the existing collection. Hence creating a new hashset.
     */
    private Set<SupportedAdditionalClusterFeature> getAdditionalClusterFeaturesAdded() {
        Set<SupportedAdditionalClusterFeature> featuresSupported =
                new HashSet<>(newCluster.getAddtionalFeaturesSupported());
        featuresSupported.removeAll(clusterFeatureDao.getAllByClusterId(newCluster.getId()));
        return featuresSupported;
    }

    private boolean isClusterFeaturesSupported(List<VDS> vdss,
                                               Set<SupportedAdditionalClusterFeature> newFeaturesEnabled) {
        Set<String> featuresNamesEnabled = new HashSet<>();
        for (SupportedAdditionalClusterFeature feature : newFeaturesEnabled) {
            featuresNamesEnabled.add(feature.getFeature().getName());
        }

        for (VDS vds : vdss) {
            Set<String> featuresSupportedByVds = hostFeatureDao.getSupportedHostFeaturesByHostId(vds.getId());
            if (!featuresSupportedByVds.containsAll(featuresNamesEnabled)) {
                return false;
            }
        }

        return true;
    }

    /**
     * New Features cannot be enabled if all up hosts are not supporting the selected feature
     */
    public ValidationResult updateSupportedFeatures() {
        List<VDS> upVdss = upVdss();
        Set<SupportedAdditionalClusterFeature> additionalClusterFeaturesAdded =
                getAdditionalClusterFeaturesAdded();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_UPDATE_SUPPORTED_FEATURES_WITH_LOWER_HOSTS)
                .when(CollectionUtils.isNotEmpty(additionalClusterFeaturesAdded)
                        && !isClusterFeaturesSupported(upVdss, additionalClusterFeaturesAdded));
    }

    public ValidationResult canChangeStoragePool() {
        Guid oldStoragePoolId = cluster.getStoragePoolId();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_CHANGE_STORAGE_POOL)
                .when(oldStoragePoolId != null
                        && !oldStoragePoolId.equals(newCluster.getStoragePoolId()));
    }

    public ValidationResult oneServiceEnabled() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED)
                .when(!newCluster.supportsGlusterService() && !newCluster.supportsVirtService());
    }

    protected boolean isAllowClusterWithVirtGluster() {
        return Config.<Boolean> getValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
    }

    public ValidationResult disableVirt() {
        boolean hasVms = !allVmsForCluster.get().isEmpty();
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS)
                .when(hasVms && !newCluster.supportsVirtService());
    }

    public ValidationResult disableGluster() {
        List<GlusterVolumeEntity> volumes = glusterVolumeDao.getByClusterId(newCluster.getId());
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES)
                .when(!newCluster.supportsGlusterService() && volumes != null && !volumes.isEmpty());
    }

    public ValidationResult setTrustedAttestation() {
        String attestationServer = Config.<String> getValue(ConfigValues.AttestationServer);
        return ValidationResult.failWith(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED)
                .when(newCluster.supportsTrustedService() && attestationServer.equals(""));
    }

    protected ArchitectureType getArchitecture() {
        Cluster eCluster = newCluster != null ? newCluster : cluster;
        if (StringUtils.isNotEmpty(eCluster.getCpuName())) {
            return cpuFlagsManagerHandler.getArchitectureByCpuName(eCluster.getCpuName(),
                    eCluster.getCompatibilityVersion());
        } else if (eCluster.getArchitecture() == null) {
            return ArchitectureType.undefined;
        }

        return eCluster.getArchitecture();
    }

    public ValidationResult migrationOnError(ArchitectureType architectureType) {
        return ValidationResult.failWith(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED)
                .when(!FeatureSupported.isMigrationSupported(architectureType, newCluster.getCompatibilityVersion())
                        && newCluster.getMigrateOnError() != MigrateOnErrorOptions.NO);
    }

    public ValidationResult implicitAffinityGroup() {
        return ValidationResult.failWith(EngineMessage.CLUSTER_IMPLICIT_AFFINITY_GROUP_IS_NOT_SUPPORTED)
                .when(!FeatureSupported.isImplicitAffinityGroupSupported(newCluster.getCompatibilityVersion())
                        && labelDao.getAllByClusterId(newCluster.getId()).stream().anyMatch(Label::isImplicitAffinityGroup));
    }

    protected boolean migrationSupportedForArch(ArchitectureType arch) {
        return FeatureSupported.isMigrationSupported(arch, cluster.getCompatibilityVersion());
    }

    public ValidationResult nonDefaultBiosType() {
        Cluster eCluster = newCluster != null ? newCluster : cluster;
        ArchitectureType architecture = getArchitecture();
        return ValidationResult.failWith(EngineMessage.NON_DEFAULT_BIOS_TYPE_FOR_X86_ONLY)
                .when(FeatureSupported.isBiosTypeSupported(eCluster.getCompatibilityVersion())
                    && eCluster.getBiosType() != null
                    && eCluster.getBiosType() != BiosType.I440FX_SEA_BIOS
                    && architecture.getFamily() != ArchitectureType.x86);
    }

    private boolean attestationServerEnabled() {
        String attestationServer = Config.getValue(ConfigValues.AttestationServer);
        return StringUtils.isNotEmpty(attestationServer);
    }

    private StoragePool getDataCenter() {
        if (dataCenter == null && cluster.getStoragePoolId() != null) {
            dataCenter = dataCenterDao.get(cluster.getStoragePoolId());
        }
        return dataCenter;
    }

    private StoragePool getDataCenterOfNewCluster() {
        if (dataCenterOfNewCluster == null && newCluster.getStoragePoolId() != null) {
            dataCenterOfNewCluster = dataCenterDao.get(newCluster.getStoragePoolId());
        }
        return dataCenterOfNewCluster;
    }

    public List<String> getLowDeviceSpaceVolumes() {
        List<String> volumes = new ArrayList<>();
        if(cluster.supportsGlusterService()) {
            List<GlusterVolumeEntity> glusterVolumeEntities = glusterVolumeDao.getByClusterId(cluster.getId());
            if(glusterVolumeEntities != null && !glusterVolumeEntities.isEmpty()) {
                for(GlusterVolumeEntity glusterVolumeEntity: glusterVolumeEntities) {
                    if (((glusterVolumeEntity.getAdvancedDetails().getCapacityInfo().getUsedSize().doubleValue()
                            / glusterVolumeEntity.getAdvancedDetails().getCapacityInfo().getTotalSize().doubleValue())
                            * 100) > (Integer)Config.getValue(ConfigValues.StorageDeviceSpaceLimit)) {
                        volumes.add(glusterVolumeEntity.getName());
                    }
                }
            }
        }
        return volumes;
    }
}
