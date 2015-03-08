package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.utils.VersionUtils.greaterOrEqual;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.KSM;
import org.ovirt.engine.api.model.MemoryOverCommit;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyThresholds;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class ClusterMapper {
    private static final String CPU_OVER_COMMIT_DURATION_MINUTES = "CpuOverCommitDurationMinutes";
    private static final String HIGH_UTILIZATION = "HighUtilization";
    private static final String LOW_UTILIZATION = "LowUtilization";
    static final org.ovirt.engine.core.compat.Version min_thp_version = new org.ovirt.engine.core.compat.Version(3, 0);

    @Mapping(from = Cluster.class, to = VDSGroup.class)
    public static VDSGroup map(Cluster model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
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
        if (model.isSetCpu() && model.getCpu().isSetId()) {
            entity.setCpuName(model.getCpu().getId());
        }
        if (model.isSetCpu() && model.getCpu().isSetArchitecture()) {
            Architecture archType = Architecture.fromValue(model.getCpu().getArchitecture());

            if (archType != null) {
                entity.setArchitecture(CPUMapper.map(archType, null));
            }
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        if (model.isSetVersion() && model.getVersion().getMajor()!=null && model.getVersion().getMinor()!=null) {
            entity.setCompatibilityVersion(new org.ovirt.engine.core.compat.Version(model.getVersion().getMajor(),
                    model.getVersion().getMinor()));
        }
        if (model.isSetMemoryPolicy()) {
            entity = map(model.getMemoryPolicy(), entity);
        } else if (model.isSetVersion() && model.getVersion().getMajor() != null
                && model.getVersion().getMinor() != null && greaterOrEqual(model.getVersion(), min_thp_version)) {
            entity.setTransparentHugepages(true);
        }
        if (model.isSetSchedulingPolicy()) {
            entity = map(model.getSchedulingPolicy(), entity);
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
        if (model.isSetOptionalReason()) {
            entity.setOptionalReasonRequired(model.isOptionalReason());
        }
        if (model.isSetMaintenanceReasonRequired()) {
            entity.setMaintenanceReasonRequired(model.isMaintenanceReasonRequired());
        }
        if (model.isSetBallooningEnabled()) {
            entity.setEnableBallooning(model.isBallooningEnabled());
        }
        if (model.isSetKsm() && model.getKsm().isSetEnabled()) {
            entity.setEnableKsm(model.getKsm().isEnabled());
        }
        if (model.isSetDisplay() && model.getDisplay().isSetProxy()) {
            entity.setSpiceProxy("".equals(model.getDisplay().getProxy()) ? null : model.getDisplay().getProxy());
        }
        if (model.isSetSerialNumber()) {
            SerialNumberMapper.copySerialNumber(model.getSerialNumber(), entity);
        }
        if (model.isSetRequiredRngSources()) {
            entity.getRequiredRngSources().clear();
            entity.getRequiredRngSources().addAll(RngDeviceMapper.mapRngSources(model.getRequiredRngSources(), null));
        }
        if (model.isSetFencingPolicy()) {
            entity.setFencingPolicy(FencingPolicyMapper.map(model.getFencingPolicy(), null));
        }
        if (model.isSetMigration()) {
            MigrationOptionsMapper.copyMigrationOptions(model.getMigration(), entity);
        }

        return entity;
    }

    @Mapping(from = VDSGroup.class, to = Cluster.class)
    public static Cluster map(VDSGroup entity, Cluster template) {
        Cluster model = template != null ? template : new Cluster();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setComment(entity.getComment());
        if (entity.getCpuName() != null) {
            CPU cpu = new CPU();
            cpu.setId(entity.getCpuName());

            cpu.setArchitecture(CPUMapper.map(entity.getArchitecture(), null));

            model.setCpu(cpu);
        }
        if (entity.getStoragePoolId() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getStoragePoolId().toString());
            model.setDataCenter(dataCenter);
        }
        if (entity.getCompatibilityVersion() != null) {
            model.setVersion(new Version());
            model.getVersion().setMajor(entity.getCompatibilityVersion().getMajor());
            model.getVersion().setMinor(entity.getCompatibilityVersion().getMinor());
        }
        model.setMemoryPolicy(map(entity, (MemoryPolicy)null));
        model.setSchedulingPolicy(map(entity, (SchedulingPolicy) null));
        model.setErrorHandling(map(entity.getMigrateOnError(), (ErrorHandling)null));
        model.setVirtService(entity.supportsVirtService());
        model.setGlusterService(entity.supportsGlusterService());
        model.setThreadsAsCores(entity.getCountThreadsAsCores());
        model.setTunnelMigration(entity.isTunnelMigration());
        model.setTrustedService(entity.supportsTrustedService());
        model.setHaReservation(entity.supportsHaReservation());
        model.setOptionalReason(entity.isOptionalReasonRequired());
        model.setMaintenanceReasonRequired(entity.isMaintenanceReasonRequired());
        model.setBallooningEnabled(entity.isEnableBallooning());
        model.setKsm(new KSM());
        model.getKsm().setEnabled(entity.isEnableKsm());
        if (StringUtils.isNotBlank(entity.getSpiceProxy())) {
            Display display = new Display();
            display.setProxy(entity.getSpiceProxy());
            model.setDisplay(display);
        }
        if (entity.getSerialNumberPolicy() != null) {
            model.setSerialNumber(SerialNumberMapper.map(entity, null));
        }

        if (entity.getRequiredRngSources() != null) {
            model.setRequiredRngSources(RngDeviceMapper.mapRngSources(entity.getRequiredRngSources(), null));
        }
        model.setMigration(MigrationOptionsMapper.map(entity, null));

        if (entity.getFencingPolicy() != null) {
            model.setFencingPolicy(FencingPolicyMapper.map(entity.getFencingPolicy(), null));
        }
        return model;
    }

    @Mapping(from = MemoryPolicy.class, to = VDSGroup.class)
    public static VDSGroup map(MemoryPolicy model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
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

    @Mapping(from = VDSGroup.class, to = MemoryPolicy.class)
    public static MemoryPolicy map(VDSGroup entity, MemoryPolicy template) {
        MemoryPolicy model = template != null ? template : new MemoryPolicy();
        model.setOverCommit(new MemoryOverCommit());
        model.getOverCommit().setPercent(entity.getMaxVdsMemoryOverCommit());
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(entity.getTransparentHugepages());
        return model;
    }

    @Mapping(from = SchedulingPolicy.class, to = VDSGroup.class)
    public static VDSGroup map(SchedulingPolicy model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
        if (model.isSetPolicy() || model.isSetName()) {
            entity.setClusterPolicyName(model.isSetName() ? model.getName() : model.getPolicy());
            entity.setClusterPolicyId(null);
        }
        // id will override name
        if (model.isSetId()) {
            entity.setClusterPolicyId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetThresholds()) {
            SchedulingPolicyThresholds thresholds = model.getThresholds();
            if (entity.getClusterPolicyProperties() == null) {
                entity.setClusterPolicyProperties(new LinkedHashMap<String, String>());
            }
            if (thresholds.getLow() != null) {
                entity.getClusterPolicyProperties().put(LOW_UTILIZATION, thresholds.getLow().toString());
            }
            if (thresholds.getHigh() != null) {
                entity.getClusterPolicyProperties().put(HIGH_UTILIZATION, thresholds.getHigh().toString());
            }
            if (thresholds.getDuration() != null) {
                int round = Math.round(thresholds.getDuration() / 60.0f);
                entity.getClusterPolicyProperties().put(CPU_OVER_COMMIT_DURATION_MINUTES, Integer.toString(round));
            }
        }
        // properties will override thresholds
        if (model.isSetProperties()) {
            entity.setClusterPolicyProperties(CustomPropertiesParser.toMap(model.getProperties()));
        }
        return entity;
    }

    @Mapping(from = VDSGroup.class, to = SchedulingPolicy.class)
    public static SchedulingPolicy map(VDSGroup entity, SchedulingPolicy template) {
        SchedulingPolicy model = template != null ? template : new SchedulingPolicy();
        if (entity.getClusterPolicyName() != null) {
            model.setId(entity.getClusterPolicyId() != null ? entity.getClusterPolicyId().toString() : null);
            model.setPolicy(entity.getClusterPolicyName().toLowerCase());
            model.setName(entity.getClusterPolicyName().toLowerCase());
            if (entity.getClusterPolicyProperties() != null && !entity.getClusterPolicyProperties().isEmpty()) {
                model.setThresholds(new SchedulingPolicyThresholds());
                String lowUtilization = entity.getClusterPolicyProperties().get(LOW_UTILIZATION);
                String highUtilization = entity.getClusterPolicyProperties().get(HIGH_UTILIZATION);
                String cpuOverCommitDurationMinutes =
                        entity.getClusterPolicyProperties().get(CPU_OVER_COMMIT_DURATION_MINUTES);
                if (lowUtilization != null) {
                    model.getThresholds().setLow(Integer.parseInt(lowUtilization));
                }
                if (highUtilization != null) {
                    model.getThresholds().setHigh(Integer.parseInt(highUtilization));
                }
                if (cpuOverCommitDurationMinutes != null) {
                    int duration = Integer.parseInt(cpuOverCommitDurationMinutes) * 60;
                    model.getThresholds().setDuration(duration);
                }
            }
        }
        if (entity.getClusterPolicyProperties() != null && !entity.getClusterPolicyProperties().isEmpty()) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getClusterPolicyProperties()));
        }
        return model;
    }

    @Mapping(from = StoragePool.class, to = VDSGroup.class)
    public static VDSGroup map(StoragePool pool, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
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
            assert(false) : "unknown migrate-on-error value: " + model.toString();
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
            assert(false) : "unknown migrate-on-error value: " + model.toString();
            return null;
        }
    }

    @Mapping(from = String.class, to = MigrateOnErrorOptions.class)
    private static MigrateOnErrorOptions map(String migrateOnError, MigrateOnErrorOptions template) {
        try {
            MigrateOnError value = MigrateOnError.fromValue(migrateOnError);
            return map(value, template);
        } catch (IllegalArgumentException e) {
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
            template.setOnError(value.value());
            return template;
        }
    }
}
