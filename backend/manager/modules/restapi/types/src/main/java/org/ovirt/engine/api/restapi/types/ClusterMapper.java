package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.utils.VersionUtils.greaterOrEqual;

import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.MemoryOverCommit;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyThresholds;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.StoragePool;

public class ClusterMapper {

    static final org.ovirt.engine.core.compat.Version min_thp_version = new org.ovirt.engine.core.compat.Version(3,0);

    @Mapping(from = Cluster.class, to = VDSGroup.class)
    public static VDSGroup map(Cluster model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetCpu() && model.getCpu().isSetId()) {
            entity.setcpu_name(model.getCpu().getId());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        if (model.isSetVersion() && model.getVersion().getMajor()!=null && model.getVersion().getMinor()!=null) {
            entity.setcompatibility_version(new org.ovirt.engine.core.compat.Version(model.getVersion().getMajor(),
                                                                                model.getVersion().getMinor()));
        }
        if (model.isSetMemoryPolicy()) {
            entity = map(model.getMemoryPolicy(), entity);
        } else if (model.isSetVersion() && greaterOrEqual(model.getVersion() , min_thp_version)){
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
        return entity;
    }

    @Mapping(from = VDSGroup.class, to = Cluster.class)
    public static Cluster map(VDSGroup entity, Cluster template) {
        Cluster model = template != null ? template : new Cluster();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        if (entity.getcpu_name() != null) {
            CPU cpu = new CPU();
            cpu.setId(entity.getcpu_name());
            model.setCpu(cpu);
        }
        if (entity.getStoragePoolId() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getStoragePoolId().toString());
            model.setDataCenter(dataCenter);
        }
        if (entity.getcompatibility_version() != null) {
            model.setVersion(new Version());
            model.getVersion().setMajor(entity.getcompatibility_version().getMajor());
            model.getVersion().setMinor(entity.getcompatibility_version().getMinor());
        }
        model.setMemoryPolicy(map(entity, (MemoryPolicy)null));
        model.setSchedulingPolicy(map(entity, (SchedulingPolicy)null));
        model.setErrorHandling(map(entity.getMigrateOnError(), (ErrorHandling)null));
        model.setVirtService(entity.supportsVirtService());
        model.setGlusterService(entity.supportsGlusterService());
        model.setThreadsAsCores(entity.getCountThreadsAsCores());
        model.setTunnelMigration(entity.isTunnelMigration());
        return model;
    }

    @Mapping(from = MemoryPolicy.class, to = VDSGroup.class)
    public static VDSGroup map(MemoryPolicy model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
        if (model.isSetOverCommit() && model.getOverCommit().getPercent()!=null) {
            entity.setmax_vds_memory_over_commit(model.getOverCommit().getPercent());
        }
        if (model.isSetTransparentHugepages() &&
            model.getTransparentHugepages().isSetEnabled()) {
            entity.setTransparentHugepages(model.getTransparentHugepages().isEnabled());
        } else if (template != null && greaterOrEqual(template.getcompatibility_version(), min_thp_version)){
            entity.setTransparentHugepages(true);
        }
        return entity;
    }

    @Mapping(from = VDSGroup.class, to = MemoryPolicy.class)
    public static MemoryPolicy map(VDSGroup entity, MemoryPolicy template) {
        MemoryPolicy model = template != null ? template : new MemoryPolicy();
        model.setOverCommit(new MemoryOverCommit());
        model.getOverCommit().setPercent(entity.getmax_vds_memory_over_commit());
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(entity.getTransparentHugepages());
        return model;
    }

    @Mapping(from = SchedulingPolicy.class, to = VDSGroup.class)
    public static VDSGroup map(SchedulingPolicy model, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
        if (model.isSetPolicy()) {
            SchedulingPolicyType policyType = SchedulingPolicyType.fromValue(model.getPolicy());
            if (policyType != null) {
                entity.setselection_algorithm(map(policyType, null));
            }
        }
        if (model.isSetThresholds()) {
            SchedulingPolicyThresholds thresholds = model.getThresholds();
            if (thresholds.getLow()!=null) {
                entity.setlow_utilization(thresholds.getLow());
            }
            if (thresholds.getHigh()!=null) {
                entity.sethigh_utilization(thresholds.getHigh());
            }
            if (thresholds.getDuration()!=null) {
                entity.setcpu_over_commit_duration_minutes(Math.round(thresholds.getDuration() / 60.0f));
            }
        }
        return entity;
    }

    @Mapping(from = VDSGroup.class, to = SchedulingPolicy.class)
    public static SchedulingPolicy map(VDSGroup entity, SchedulingPolicy template) {
        SchedulingPolicy model = template != null ? template : new SchedulingPolicy();
        if (entity.getselection_algorithm() != null) {
            model.setPolicy(map(entity.getselection_algorithm(), null));
            if (model.isSetPolicy()) {
                model.setThresholds(new SchedulingPolicyThresholds());
                switch (entity.getselection_algorithm()) {
                case PowerSave:
                    model.getThresholds().setLow(entity.getlow_utilization());
                case EvenlyDistribute:
                    model.getThresholds().setHigh(entity.gethigh_utilization());
                    model.getThresholds().setDuration(entity.getcpu_over_commit_duration_minutes() * 60);
                    break;
                default:
                    break;
                }
            }
        }
        return model;
    }

    @Mapping(from = SchedulingPolicyType.class, to = VdsSelectionAlgorithm.class)
    public static VdsSelectionAlgorithm map(SchedulingPolicyType model, VdsSelectionAlgorithm template) {
        switch (model) {
        case POWER_SAVING:       return VdsSelectionAlgorithm.PowerSave;
        case EVENLY_DISTRIBUTED: return VdsSelectionAlgorithm.EvenlyDistribute;
        case NONE:               return VdsSelectionAlgorithm.None;
        default:                 return null;
        }
    }

    @Mapping(from = VdsSelectionAlgorithm.class, to = String.class)
    public static String map(VdsSelectionAlgorithm entity, String template) {
        switch (entity) {
        case PowerSave:        return SchedulingPolicyType.POWER_SAVING.value();
        case EvenlyDistribute: return SchedulingPolicyType.EVENLY_DISTRIBUTED.value();
        case None:             return null;
        default:               return null;
        }
    }

    @Mapping(from = StoragePool.class, to = VDSGroup.class)
    public static VDSGroup map(StoragePool pool, VDSGroup template) {
        VDSGroup entity = template != null ? template : new VDSGroup();
        if (pool.getcompatibility_version() != null) {
            entity.setcompatibility_version(pool.getcompatibility_version());
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
