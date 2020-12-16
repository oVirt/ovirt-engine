package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.utils.VersionUtils.greaterOrEqual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Cluster.RequiredRngSourcesList;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.model.FipsMode;
import org.ovirt.engine.api.model.Ksm;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.MemoryOverCommit;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.compat.Guid;

public class ClusterMapper {
    static final org.ovirt.engine.core.compat.Version min_thp_version = new org.ovirt.engine.core.compat.Version(3, 0);

    @Mapping(from = org.ovirt.engine.api.model.Cluster.class, to = Cluster.class)
    public static Cluster map(org.ovirt.engine.api.model.Cluster model, Cluster template) {
        Cluster entity = template != null ? template : new Cluster();

        if (model.isSetSwitchType()) {
            entity.setRequiredSwitchTypeForCluster(SwitchTypeMapper.mapFromModel(model.getSwitchType()));
        }

        if (model.isSetFirewallType()) {
            entity.setFirewallType(FirewallTypeMapper.mapFromModel(model.getFirewallType()));
        }

        if (model.isSetLogMaxMemoryUsedThresholdType()) {
            entity.setLogMaxMemoryUsedThresholdType(
                    LogMaxMemoryUsedThresholdTypeMapper.mapFromModel(model.getLogMaxMemoryUsedThresholdType()));
            entity.setLogMaxMemoryUsedThreshold(model.getLogMaxMemoryUsedThreshold());
        }

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetCpu() && model.getCpu().isSetType()) {
            entity.setCpuName(model.getCpu().getType());
        }
        if (model.isSetCpu() && model.getCpu().isSetArchitecture()) {
            entity.setArchitecture(CPUMapper.map(model.getCpu().getArchitecture(), null));
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        if (model.isSetVersion() && model.getVersion().getMajor() != null && model.getVersion().getMinor() != null) {
            entity.setCompatibilityVersion(VersionMapper.map(model.getVersion()));
        }
        if (model.isSetBiosType()) {
            entity.setBiosType(VmBaseMapper.map(model.getBiosType(), null));
        }
        if (model.isSetMemoryPolicy()) {
            entity = map(model.getMemoryPolicy(), entity);
        } else if (model.isSetVersion() && model.getVersion().getMajor() != null
                && model.getVersion().getMinor() != null && greaterOrEqual(model.getVersion(), min_thp_version)) {
            entity.setTransparentHugepages(true);
        }
        SchedulingPolicy schedulingPolicy = model.getSchedulingPolicy();
        if (schedulingPolicy != null) {
            if (schedulingPolicy.isSetName()) {
                entity.setClusterPolicyName(schedulingPolicy.getName());
            }
            if (schedulingPolicy.isSetId()) {
                entity.setClusterPolicyId(GuidUtils.asGuid(schedulingPolicy.getId()));
            }
        }
        if (model.isSetErrorHandling() && model.getErrorHandling().isSetOnError()) {
            entity.setMigrateOnError(map(model.getErrorHandling().getOnError(), null));
        }
        if(model.isSetVirtService()) {
            entity.setVirtService(model.isVirtService());
        }
        if(model.isSetGlusterService()) {
            entity.setGlusterService(model.isGlusterService());
        }
        if (model.isSetThreadsAsCores()) {
            entity.setCountThreadsAsCores(model.isThreadsAsCores());
        }
        if (model.isSetTunnelMigration()) {
            entity.setTunnelMigration(model.isTunnelMigration());
        }
        if (model.isSetTrustedService()){
            entity.setTrustedService(model.isTrustedService());
        }
        if (model.isSetHaReservation()) {
            entity.setHaReservation(model.isHaReservation());
        }

        if (model.isSetBallooningEnabled()) {
            entity.setEnableBallooning(model.isBallooningEnabled());
        }
        if (model.isSetKsm()) {
            if (model.getKsm().isSetEnabled()) {
                entity.setEnableKsm(model.getKsm().isEnabled());
            }
            if (model.getKsm().isSetMergeAcrossNodes()) {
                entity.setKsmMergeAcrossNumaNodes(model.getKsm().isMergeAcrossNodes());
            }
        }
        if (model.isSetDisplay() && model.getDisplay().isSetProxy()) {
            entity.setSpiceProxy("".equals(model.getDisplay().getProxy()) ? null : model.getDisplay().getProxy());
        }
        if (model.isSetSerialNumber()) {
            SerialNumberMapper.copySerialNumber(model.getSerialNumber(), entity);
        }
        /*
         * For backward compatibility additional rng sources are presented in <required_rng_sources> together with
         * implicit /dev/urandom or /dev/random source. <required_rng_sources> should be changed to
         * <additional_rng_sources> during next api change.
         */
        if (model.isSetRequiredRngSources()) {
            entity.getAdditionalRngSources().clear();
            entity.getAdditionalRngSources().addAll(RngDeviceMapper.mapRngSources(model.getRequiredRngSources().getRequiredRngSources()));
            entity.getAdditionalRngSources().remove(VmRngDevice.Source.RANDOM);
            entity.getAdditionalRngSources().remove(VmRngDevice.Source.URANDOM);
        }
        if (model.isSetFencingPolicy()) {
            entity.setFencingPolicy(FencingPolicyMapper.map(model.getFencingPolicy(), null));
        }
        if (model.isSetMigration()) {
            ClusterMigrationOptionsMapper.copyMigrationOptions(model.getMigration(), entity);
        }

        if (model.isSetMacPool() && model.getMacPool().isSetId()) {
            entity.setMacPoolId(GuidUtils.asGuid(model.getMacPool().getId()));
        }

        // properties will override thresholds
        if (model.isSetCustomSchedulingPolicyProperties()) {
            Map<String, String> properties = entity.getClusterPolicyProperties();
            if (properties == null) {
                properties = new HashMap<>();
                entity.setClusterPolicyProperties(properties);
            }
            Map<String, String> customProperties =
                    CustomPropertiesParser.toMap(model.getCustomSchedulingPolicyProperties());
            properties.putAll(customProperties);
        }

        if (model.isSetGlusterTunedProfile()) {
            entity.setGlusterTunedProfile(model.getGlusterTunedProfile());
        }

        if (model.isSetExternalNetworkProviders()) {
            List<ExternalProvider> externalNetworkProviders =
                    model.getExternalNetworkProviders().getExternalProviders();
            if (externalNetworkProviders.size() == 0) {
                entity.setDefaultNetworkProviderId(null);
            } else {
                // Ignore everything but the first external provider, because engine's Cluster currently supports
                // only a single external network provider
                String providerId = externalNetworkProviders.get(0).getId();
                entity.setDefaultNetworkProviderId(providerId == null ? null : GuidUtils.asGuid(providerId));
            }
        }

        if (model.isSetVncEncryption()) {
            entity.setVncEncryptionEnabled(model.isVncEncryption());
        }
        if (model.isSetFipsMode() && model.getFipsMode() != null) {
            entity.setFipsMode(map(model.getFipsMode(), null));
        }
        return entity;
    }

    @Mapping(from = Cluster.class, to = org.ovirt.engine.api.model.Cluster.class)
    public static org.ovirt.engine.api.model.Cluster map(Cluster entity, org.ovirt.engine.api.model.Cluster template) {
        org.ovirt.engine.api.model.Cluster model = template != null ? template : new org.ovirt.engine.api.model.Cluster();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setComment(entity.getComment());
        model.setSwitchType(SwitchTypeMapper.mapToModel(entity.getRequiredSwitchTypeForCluster()));
        model.setFirewallType(FirewallTypeMapper.mapToModel(entity.getFirewallType()));
        model.setLogMaxMemoryUsedThresholdType(
                LogMaxMemoryUsedThresholdTypeMapper.mapToModel(entity.getLogMaxMemoryUsedThresholdType()));
        model.setLogMaxMemoryUsedThreshold(entity.getLogMaxMemoryUsedThreshold());

        if (entity.getCpuName() != null) {
            Cpu cpu = new Cpu();
            cpu.setType(entity.getCpuName());

            cpu.setArchitecture(CPUMapper.map(entity.getArchitecture(), null));

            model.setCpu(cpu);
        }
        model.setBiosType(VmBaseMapper.map(entity.getBiosType(), null));
        if (entity.getStoragePoolId() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getStoragePoolId().toString());
            model.setDataCenter(dataCenter);
        }
        if (entity.getCompatibilityVersion() != null) {
            model.setVersion(VersionMapper.map(entity.getCompatibilityVersion()));
        }
        model.setMemoryPolicy(map(entity, (MemoryPolicy)null));
        Guid clusterPolicyId = entity.getClusterPolicyId();
        if (clusterPolicyId != null) {
            SchedulingPolicy schedulingPolicy = model.getSchedulingPolicy();
            if (schedulingPolicy == null) {
                schedulingPolicy = new SchedulingPolicy();
                model.setSchedulingPolicy(schedulingPolicy);
            }
            schedulingPolicy.setId(clusterPolicyId.toString());
        }
        model.setErrorHandling(map(entity.getMigrateOnError(), (ErrorHandling)null));
        model.setVirtService(entity.supportsVirtService());
        model.setGlusterService(entity.supportsGlusterService());
        model.setThreadsAsCores(entity.getCountThreadsAsCores());
        model.setTunnelMigration(entity.isTunnelMigration());
        model.setTrustedService(entity.supportsTrustedService());
        model.setHaReservation(entity.supportsHaReservation());
        model.setBallooningEnabled(entity.isEnableBallooning());
        model.setVncEncryption(entity.isVncEncryptionEnabled());
        Ksm ksm = model.getKsm();
        if (ksm == null) {
            ksm = new Ksm();
            model.setKsm(ksm);
        }
        ksm.setEnabled(entity.isEnableKsm());
        ksm.setMergeAcrossNodes(entity.isKsmMergeAcrossNumaNodes());
        if (StringUtils.isNotBlank(entity.getSpiceProxy())) {
            Display display = new Display();
            display.setProxy(entity.getSpiceProxy());
            model.setDisplay(display);
        }
        if (entity.getSerialNumberPolicy() != null) {
            model.setSerialNumber(SerialNumberMapper.map(entity, null));
        }

        if (entity.getRequiredRngSources() != null) {
            model.setRequiredRngSources(new RequiredRngSourcesList());
            model.getRequiredRngSources().getRequiredRngSources().addAll(RngDeviceMapper.mapRngSources(entity.getRequiredRngSources()));
        }
        model.setMigration(ClusterMigrationOptionsMapper.map(entity, null));

        if (entity.getFencingPolicy() != null) {
            model.setFencingPolicy(FencingPolicyMapper.map(entity.getFencingPolicy(), null));
        }

        if (entity.getMacPoolId() != null) {
            MacPool macPool = model.getMacPool();
            if (macPool == null) {
                macPool = new MacPool();
                model.setMacPool(macPool);
            }

            macPool.setId(entity.getMacPoolId().toString());
        }

        if (entity.getClusterPolicyProperties() != null && !entity.getClusterPolicyProperties().isEmpty()) {
            model.setCustomSchedulingPolicyProperties(CustomPropertiesParser.fromMap(entity
                    .getClusterPolicyProperties()));
        }

        if (entity.getGlusterTunedProfile() != null && !entity.getGlusterTunedProfile().isEmpty()) {
            model.setGlusterTunedProfile(entity.getGlusterTunedProfile());
        }
        model.setFipsMode(map(entity.getFipsMode(), null));

        return model;
    }

    @Mapping(from = MemoryPolicy.class, to = Cluster.class)
    public static Cluster map(MemoryPolicy model, Cluster template) {
        Cluster entity = template != null ? template : new Cluster();
        if (model.isSetOverCommit() && model.getOverCommit().getPercent()!=null) {
            entity.setMaxVdsMemoryOverCommit(model.getOverCommit().getPercent());
        }
        if (model.isSetTransparentHugepages() &&
            model.getTransparentHugepages().isSetEnabled()) {
            entity.setTransparentHugepages(model.getTransparentHugepages().isEnabled());
        } else if (template != null && greaterOrEqual(template.getCompatibilityVersion(), min_thp_version)){
            entity.setTransparentHugepages(true);
        }
        return entity;
    }

    @Mapping(from = Cluster.class, to = MemoryPolicy.class)
    public static MemoryPolicy map(Cluster entity, MemoryPolicy template) {
        MemoryPolicy model = template != null ? template : new MemoryPolicy();
        model.setOverCommit(new MemoryOverCommit());
        model.getOverCommit().setPercent(entity.getMaxVdsMemoryOverCommit());
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(entity.getTransparentHugepages());
        return model;
    }

    @Mapping(from = StoragePool.class, to = Cluster.class)
    public static Cluster map(StoragePool pool, Cluster template) {
        Cluster entity = template != null ? template : new Cluster();
        if (pool.getCompatibilityVersion() != null) {
            entity.setCompatibilityVersion(pool.getCompatibilityVersion());
        }
        return entity;
    }

    @Mapping(from = MigrateOnError.class, to = MigrateOnErrorOptions.class)
    public static MigrateOnErrorOptions map(MigrateOnError model, MigrateOnErrorOptions template) {
        if (model==null) {
            return null;
        }
        switch (model) {
        case MIGRATE:
            return MigrateOnErrorOptions.YES;
        case DO_NOT_MIGRATE:
            return MigrateOnErrorOptions.NO;
        case MIGRATE_HIGHLY_AVAILABLE:
            return MigrateOnErrorOptions.HA_ONLY;
        default:
            assert false : "unknown migrate-on-error value: " + model.toString();
            return null;
        }
    }

    @Mapping(from = MigrateOnErrorOptions.class, to = MigrateOnError.class)
    public static MigrateOnError map(MigrateOnErrorOptions model, MigrateOnError template) {
        if (model==null) {
            return null;
        }
        switch (model) {
        case YES:
            return MigrateOnError.MIGRATE;
        case NO:
            return MigrateOnError.DO_NOT_MIGRATE;
        case HA_ONLY:
            return MigrateOnError.MIGRATE_HIGHLY_AVAILABLE;
        default:
            assert false : "unknown migrate-on-error value: " + model.toString();
            return null;
        }
    }

    @Mapping(from = MigrateOnErrorOptions.class, to = ErrorHandling.class)
    private static ErrorHandling map(MigrateOnErrorOptions migrateOnError, ErrorHandling template) {
        MigrateOnError value = map(migrateOnError, (MigrateOnError)null);
        if (value==null) {
            return null;
        } else {
            template = template==null ? new ErrorHandling() : template;
            template.setOnError(value);
            return template;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.FipsMode.class, to = FipsMode.class)
    public static FipsMode map(org.ovirt.engine.core.common.businessentities.FipsMode model, FipsMode template) {
        switch (model) {
            case DISABLED:
                return FipsMode.DISABLED;
            case ENABLED:
                return FipsMode.ENABLED;
            default:
                return FipsMode.UNDEFINED;
        }
    }

    @Mapping(from = FipsMode.class, to = org.ovirt.engine.core.common.businessentities.FipsMode.class)
    public static org.ovirt.engine.core.common.businessentities.FipsMode map(FipsMode model, org.ovirt.engine.core.common.businessentities.FipsMode template) {
        switch (model) {
            case DISABLED:
                return org.ovirt.engine.core.common.businessentities.FipsMode.DISABLED;
            case ENABLED:
                return org.ovirt.engine.core.common.businessentities.FipsMode.ENABLED;
            default:
                return org.ovirt.engine.core.common.businessentities.FipsMode.UNDEFINED;
        }
    }
}
