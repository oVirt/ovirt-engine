package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmStaticDaoImpl extends VmBaseDao<VmStatic> implements VmStaticDao {
    public static final Integer USE_LATEST_VERSION_NUMBER_INDICATOR = null;
    public static final Integer DONT_USE_LATEST_VERSION_NUMBER_INDICATOR = 1;

    public VmStaticDaoImpl() {
        super("VmStatic");
        setProcedureNameForGet("GetVmStaticByVmGuid");
    }

    @Override
    public List<VmStatic> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmStatic vm) {
        return createBaseParametersMapper(vm)
                .addValue("vm_name", vm.getName())
                .addValue("vmt_guid", vm.getVmtGuid())
                .addValue("is_initialized", vm.isInitialized())
                .addValue("host_cpu_flags", vm.isUseHostCpuFlags())
                .addValue("instance_type_id", vm.getInstanceTypeId())
                .addValue("image_type_id", vm.getImageTypeId())
                .addValue("original_template_name", vm.getOriginalTemplateName())
                .addValue("original_template_id", vm.getOriginalTemplateGuid())
                .addValue("template_version_number", vm.isUseLatestVersion() ?
                        USE_LATEST_VERSION_NUMBER_INDICATOR : DONT_USE_LATEST_VERSION_NUMBER_INDICATOR)
                .addValue("provider_id", vm.getProviderId())
                .addValue("namespace", vm.getNamespace());
    }

    @Override
    protected RowMapper<VmStatic> createEntityRowMapper() {
        return VMStaticRowMapper.instance;
    }

    @Override
    public void remove(Guid id) {
        remove(id, true);
    }

    public void remove(Guid id, boolean removePermissions) {
        getCallsHandler().executeModification("DeleteVmStatic",
                createIdParameterMapper(id)
                        .addValue("remove_permissions", removePermissions));
    }


    public List<Guid> getVmAndTemplatesIdsWithoutAttachedImageDisks(Guid storagePoolId, boolean shareableDisks) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId)
                .addValue("shareable", shareableDisks);
        return getCallsHandler().executeReadList("GetVmsAndTemplatesIdsWithoutAttachedImageDisks",
                createGuidMapper(), parameterSource);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    @Override
    public List<VmStatic> getAllByName(String name) {
        return getCallsHandler().executeReadList("GetVmStaticByName", VMStaticRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vm_name", name));

    }

    @Override
    public List<VmStatic> getAllByStoragePoolId(Guid spId) {
        return getCallsHandler().executeReadList("GetAllFromVmStaticByStoragePoolId",
                VMStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("sp_id", spId));
    }

    @Override
    public List<VmStatic> getAllByCluster(Guid cluster) {
        return getCallsHandler().executeReadList("GetVmStaticByCluster", VMStaticRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster));
    }

    @Override
    public List<VmStatic> getAllByGroupAndNetworkName(Guid group, String name) {
        return getCallsHandler().executeReadList("GetvmStaticByGroupIdAndNetwork", VMStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("groupId", group).addValue("networkName", name));
    }

    @Override
    public void incrementDbGenerationForAllInStoragePool(Guid storagePoolId) {
        getCallsHandler().executeModification("IncrementDbGenerationForAllInStoragePool", getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId));

    }

    @Override
    public void incrementDbGeneration(Guid id) {
        getCallsHandler().executeModification("IncrementDbGeneration", getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    @Override
    public void incrementDbGenerationForVms(List<Guid> guids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guids", createArrayOfUUIDs(guids));
        getCallsHandler().executeModification(
                "IncrementDbGenerationForVms",
                parameterSource);
    }


    @Override
    public Long getDbGeneration(Guid id) {
        return getCallsHandler().executeRead("GetDbGeneration", SingleColumnRowMapper.newInstance(Long.class),
                getCustomMapSqlParameterSource().addValue("vm_guid", id));
    }

    public List<Guid> getOrderedVmGuidsForRunMultipleActions(List<Guid> guids) {
        return getCallsHandler().executeReadList("GetOrderedVmGuidsForRunMultipleActions", createGuidMapper()
                , getCustomMapSqlParameterSource().addValue("vm_guids",
                        guids.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","))));
    }

    @Override
    public void updateVmCpuProfileIdForClusterId(Guid clusterId, Guid cpuProfileId) {
        getCallsHandler().executeModification("UpdateVmCpuProfileIdForClusterId",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("cpu_profile_id", cpuProfileId));
    }

    @Override
    public List<VmStatic> getAllWithoutIcon() {
        return getCallsHandler().executeReadList("GetVmStaticWithoutIcon",
                getRowMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<String> getAllRunningNamesWithLeaseOnStorageDomain(Guid storageDomainId) {
        return getCallsHandler().executeReadList("GetActiveVmNamesWithLeaseOnStorageDomain",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public List<String> getAllRunningNamesWithIsoOnStorageDomain(Guid storageDomainId) {
        return getCallsHandler().executeReadList("GetActiveVmNamesWithIsoOnStorageDomain",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public List<String> getAllNamesWithSpecificIsoAttached(Guid isoDiskId) {
        return getCallsHandler().executeReadList("GetVmNamesWithSpecificIsoAttached",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource().addValue("iso_disk_id", isoDiskId));
    }


    @Override
    public List<VmStatic> getAllWithLeaseOnStorageDomain(Guid storageDomainId) {
        return getCallsHandler().executeReadList("GetVmsWithLeaseOnStorageDomain",
                getRowMapper(),
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public List<VmStatic> getAllRunningForVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsStaticRunningOnVds",
                getRowMapper(),
                getCustomMapSqlParameterSource().addValue("vds_id", id));
    }

    @Override
    public void updateVmLeaseStorageDomainId(Guid vmId, Guid storageDomainId) {
        getCallsHandler().executeModification("UpdateVmLeaseStorageDomainId",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("sd_id", storageDomainId));
    }

    @Override
    public List<VmStatic> getByIds(List<Guid> ids) {
        return getCallsHandler().executeReadList("GetVmStaticByVmGuids",
                getRowMapper(),
                getCustomMapSqlParameterSource().addValue("vm_guids", createArrayOfUUIDs(ids)));
    }

    /**
     * JDBC row mapper for VM static
     */
    private static class VMStaticRowMapper extends AbstractVmRowMapper<VmStatic> {
        public static final VMStaticRowMapper instance = new VMStaticRowMapper();

        @Override
        public VmStatic mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmStatic entity = new VmStatic();
            map(rs, entity);

            entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
            entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));

            entity.setName(rs.getString("vm_name"));
            entity.setVmtGuid(getGuidDefaultEmpty(rs, "vmt_guid"));
            entity.setInitialized(rs.getBoolean("is_initialized"));
            entity.setUseHostCpuFlags(rs.getBoolean("host_cpu_flags"));
            entity.setInstanceTypeId(Guid.createGuidFromString(rs.getString("instance_type_id")));
            entity.setImageTypeId(Guid.createGuidFromString(rs.getString("image_type_id")));
            entity.setOriginalTemplateName(rs.getString("original_template_name"));
            entity.setOriginalTemplateGuid(getGuid(rs, "original_template_id"));
            // if template_version_number is null it means use latest version
            entity.setUseLatestVersion(rs.getObject("template_version_number") == USE_LATEST_VERSION_NUMBER_INDICATOR);
            entity.setProviderId(getGuid(rs, "provider_id"));
            entity.setNamespace(rs.getString("namespace"));
            return entity;
        }
    }

    public static RowMapper<VmStatic> getRowMapper() {
        return VMStaticRowMapper.instance;
    }
}
