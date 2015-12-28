package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.compat.Guid;


public class QuotaMapper {

    @Mapping(from = Quota.class, to = org.ovirt.engine.core.common.businessentities.Quota.class)
    public static org.ovirt.engine.core.common.businessentities.Quota map(Quota model, org.ovirt.engine.core.common.businessentities.Quota template) {
        org.ovirt.engine.core.common.businessentities.Quota entity = (template==null) ? new org.ovirt.engine.core.common.businessentities.Quota() : template;
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setQuotaName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetDataCenter()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        if (model.isSetClusterHardLimitPct()) {
            entity.setGraceClusterPercentage(model.getClusterHardLimitPct());
        }
        if (model.isSetStorageHardLimitPct()) {
            entity.setGraceStoragePercentage(model.getStorageHardLimitPct());
        }
        if (model.isSetClusterSoftLimitPct()) {
            entity.setThresholdClusterPercentage(model.getClusterSoftLimitPct());
        }
        if (model.isSetStorageSoftLimitPct()) {
            entity.setThresholdStoragePercentage(model.getStorageSoftLimitPct());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Quota.class, to = Quota.class)
    public static Quota map(org.ovirt.engine.core.common.businessentities.Quota template, Quota model) {
        Quota ret = (model==null) ? new Quota() : model;
        if (template.getId()!=null) {
            ret.setId(template.getId().toString());
        }
        if (template.getQuotaName()!=null) {
            ret.setName(template.getQuotaName());
        }
        if (template.getDescription()!=null) {
            ret.setDescription(template.getDescription());
        }
        if (template.getStoragePoolId()!=null) {
            if (ret.getDataCenter()==null) {
                ret.setDataCenter(new DataCenter());
            }
            ret.getDataCenter().setId(template.getStoragePoolId().toString());
        }
        ret.setClusterHardLimitPct(template.getGraceClusterPercentage());
        ret.setStorageHardLimitPct(template.getGraceStoragePercentage());
        ret.setClusterSoftLimitPct(template.getThresholdClusterPercentage());
        ret.setStorageSoftLimitPct(template.getThresholdStoragePercentage());
        return ret;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Quota.class, to = QuotaStorageLimit.class)
    public static QuotaStorageLimit map(org.ovirt.engine.core.common.businessentities.Quota entity,
            QuotaStorageLimit template) {
        QuotaStorageLimit model = template != null ? template : new QuotaStorageLimit();
        Guid guid = GuidUtils.asGuid(model.getId());
        // global
        if (guid.equals(entity.getId())) {
            map(model, entity.getGlobalQuotaStorage(), null, entity.getStoragePoolId().toString(), entity.getId()
                    .toString());
        } else { // specific
            if (entity.getQuotaStorages() != null) {
                for (QuotaStorage quotaStorage : entity.getQuotaStorages()) {
                    if (quotaStorage.getStorageId() != null && quotaStorage.getStorageId().equals(guid)) {
                        map(model, quotaStorage, quotaStorage.getStorageId().toString(), entity.getStoragePoolId()
                                .toString(), entity.getId().toString());
                    }
                }
            }
        }
        return model;
    }

    private static void map(QuotaStorageLimit model,
            QuotaStorage quotaStorage,
            String storageDomainId,
            String dataCenterId,
            String quotaId) {
        model.setQuota(new Quota());
        model.getQuota().setId(quotaId);
        model.getQuota().setDataCenter(new DataCenter());
        model.getQuota().getDataCenter().setId(dataCenterId);
        if (storageDomainId != null) {
            model.setStorageDomain(new StorageDomain());
            model.getStorageDomain().setId(storageDomainId);
        }
        if (quotaStorage.getStorageSizeGB() != null) {
            model.setLimit(quotaStorage.getStorageSizeGB());
        }
        if (quotaStorage.getStorageSizeGBUsage() != null) {
            model.setUsage(quotaStorage.getStorageSizeGBUsage());
        }
    }

    @Mapping(from = QuotaStorageLimit.class, to = org.ovirt.engine.core.common.businessentities.Quota.class)
    public static org.ovirt.engine.core.common.businessentities.Quota map(QuotaStorageLimit model,
            org.ovirt.engine.core.common.businessentities.Quota template) {
        org.ovirt.engine.core.common.businessentities.Quota entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.Quota();
        QuotaStorage quotaStorage = new QuotaStorage();
        if (model.isSetLimit()) {
            quotaStorage.setStorageSizeGB(model.getLimit());
        }
        // specific SD
        if(model.isSetStorageDomain() && model.getStorageDomain().isSetId()) {
            quotaStorage.setStorageId(GuidUtils.asGuid(model.getStorageDomain().getId()));
            entity.getQuotaStorages().add(quotaStorage);
        } else { // global
            entity.setGlobalQuotaStorage(quotaStorage);
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Quota.class, to = QuotaClusterLimit.class)
    public static QuotaClusterLimit map(org.ovirt.engine.core.common.businessentities.Quota entity,
            QuotaClusterLimit template) {
        QuotaClusterLimit model = template != null ? template : new QuotaClusterLimit();
        Guid guid = GuidUtils.asGuid(model.getId());
        // global
        if (guid.equals(entity.getId())) {
            map(model, entity.getGlobalQuotaCluster(), null, entity.getStoragePoolId().toString(), entity.getId()
                    .toString());
        } else { // specific
            if (entity.getQuotaClusters() != null) {
                for (QuotaCluster quotaCluster : entity.getQuotaClusters()) {
                    if (quotaCluster.getClusterId() != null && quotaCluster.getClusterId().equals(guid)) {
                        map(model, quotaCluster, quotaCluster.getClusterId().toString(), entity.getStoragePoolId()
                                .toString(), entity.getId().toString());
                    }
                }
            }
        }
        return model;
    }

    private static void map(QuotaClusterLimit template,
            QuotaCluster quotaCluster,
            String clusterId,
            String dataCenterId,
            String quotaId) {
        template.setQuota(new Quota());
        template.getQuota().setId(quotaId);
        template.getQuota().setDataCenter(new DataCenter());
        template.getQuota().getDataCenter().setId(dataCenterId);
        if (clusterId != null) {
            template.setCluster(new Cluster());
            template.getCluster().setId(clusterId);
        }
        if (quotaCluster.getMemSizeMB() != null) {
            // show GB instead of MB (ignore -1)
            double value = quotaCluster.getMemSizeMB() == -1 ? quotaCluster.getMemSizeMB().doubleValue()
                    : quotaCluster.getMemSizeMB().doubleValue() / 1024.0;
            template.setMemoryLimit(value);
        }
        if (quotaCluster.getMemSizeMBUsage() != null) {
            template.setMemoryUsage(quotaCluster.getMemSizeMBUsage() / 1024.0);
        }
        if (quotaCluster.getVirtualCpu() != null) {
            template.setVcpuLimit(quotaCluster.getVirtualCpu());
        }
        if (quotaCluster.getVirtualCpuUsage() != null) {
            template.setVcpuUsage(quotaCluster.getVirtualCpuUsage());
        }
    }

    @Mapping(from = QuotaClusterLimit.class, to = org.ovirt.engine.core.common.businessentities.Quota.class)
    public static org.ovirt.engine.core.common.businessentities.Quota map(QuotaClusterLimit model,
            org.ovirt.engine.core.common.businessentities.Quota template) {
        org.ovirt.engine.core.common.businessentities.Quota entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.Quota();
        QuotaCluster quotaCluster = new QuotaCluster();
        if (model.isSetVcpuLimit()) {
            quotaCluster.setVirtualCpu(model.getVcpuLimit());
        }

        if (model.isSetMemoryLimit()) {
            double limit = model.getMemoryLimit();
            quotaCluster.setMemSizeMB( (limit < 0) ? -1 : (long) (limit * 1024) );
        }

        // specific cluster
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            quotaCluster.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
            entity.getQuotaClusters().add(quotaCluster);
        } else { // global
            entity.setGlobalQuotaCluster(quotaCluster);
        }
        return entity;
    }
}
