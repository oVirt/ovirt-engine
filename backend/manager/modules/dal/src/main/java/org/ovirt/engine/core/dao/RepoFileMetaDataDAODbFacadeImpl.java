package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>StorageDomainDAODbFacadeImpl</code> provides an implementation of {@link StorageDomainDAO} based on code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}. Responsible for managing the repository files meta data via the
 * persistent layer.
 */
public class RepoFileMetaDataDAODbFacadeImpl extends BaseDAODbFacade implements RepoFileMetaDataDAO {

    @Override
    public void removeRepoDomainFileList(Guid id, ImageType filetype) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("repo_domain_id", id).addValue("file_type", filetype.getValue());

        getCallsHandler().executeModification("DeleteRepo_domain_file_list", parameterSource);
    }

    @Override
    public void addRepoFileMap(RepoFileMetaData map) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("repo_domain_id", map.getRepoDomainId())
                        .addValue("repo_file_name", map.getRepoFileName())
                        .addValue("size", map.getSize())
                        .addValue("date_created", map.getDateCreated())
                        .addValue("last_refreshed", map.getLastRefreshed())
                        .addValue("file_type", map.getFileType().getValue());

        getCallsHandler().executeModification("InsertRepo_domain_file_meta_data", parameterSource);
    }

    /**
     * Returns a list of repository files with specific file extension from storage domain id with specific status.
     */
    @Override
    public List<RepoFileMetaData> getRepoListForStorageDomainAndStoragePool(Guid storagePoolId, Guid storageDomainId,
            ImageType fileType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                storagePoolId);
        parameterSource.addValue("storage_domain_id", storageDomainId);
        parameterSource.addValue("file_type", fileType.getValue());

        return getCallsHandler().executeReadList("GetRepo_files_by_storage_domain_and_storage_pool",
                StorageDomainRepoFileMetaDataMapper.instance,
                parameterSource);
    }

    /**
     * Returns a list of repository files with specific file extension from storage domain id.<BR/>
     * If no repository is found, the method will return an empty list.
     */
    @Override
    public List<RepoFileMetaData> getRepoListForStorageDomain(Guid storageDomainId,
            ImageType fileType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_domain_id",
                storageDomainId);
        parameterSource.addValue("file_type", fileType.getValue());

        return getCallsHandler().executeReadList("GetRepo_files_by_storage_domain", RepoFileMetaDataMapper.instance,
                parameterSource);
    }

    /**
     * Returns a list of all repository files with specific file extension from all the storage pools,
     * which meet the same status, type and SPM status.
     */
    @Override
    public List<RepoFileMetaData> getAllRepoFilesForAllStoragePools(StorageDomainType storageDomainType,
            StoragePoolStatus storagePoolStatus, StorageDomainStatus storageDomainStatus,
            VDSStatus vdsStatus) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("storage_domain_type", storageDomainType.getValue());
        parameterSource.addValue("storage_pool_status", storagePoolStatus.getValue());
        parameterSource.addValue("vds_status", vdsStatus.getValue());
        parameterSource.addValue("storage_domain_status", storageDomainStatus.getValue());

        return getCallsHandler().executeReadList("GetRepo_files_in_all_storage_pools",
                ThinRepoFileMetaDataMapper.instance,
                parameterSource);
    }

    private static class ThinRepoFileMetaDataMapper implements ParameterizedRowMapper<RepoFileMetaData> {
        public static final ThinRepoFileMetaDataMapper instance = new ThinRepoFileMetaDataMapper();

        @Override
        public RepoFileMetaData mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepoFileMetaData entity = new RepoFileMetaData();
            entity.setRepoDomainId(Guid.createGuidFromString(rs.getString("storage_domain_id")));
            entity.setLastRefreshed(rs.getLong("last_refreshed"));
            entity.setFileType(ImageType.forValue(rs.getInt("file_type")));
            return entity;
        }
    }

    private static class RepoFileMetaDataMapper implements ParameterizedRowMapper<RepoFileMetaData> {
        public static final RepoFileMetaDataMapper instance = new RepoFileMetaDataMapper();

        @Override
        public RepoFileMetaData mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepoFileMetaData entity = new RepoFileMetaData();
            entity.setRepoDomainId(Guid.createGuidFromString(rs.getString("repo_domain_id")));
            entity.setRepoFileName(rs.getString("repo_file_name"));
            entity.setSize(rs.getLong("size"));
            entity.setDateCreated((Date) rs.getObject("date_created"));
            entity.setLastRefreshed(rs.getLong("last_refreshed"));
            return entity;
        }
    }

    private static class StorageDomainRepoFileMetaDataMapper implements ParameterizedRowMapper<RepoFileMetaData> {
        public static final StorageDomainRepoFileMetaDataMapper instance = new StorageDomainRepoFileMetaDataMapper();

        @Override
        public RepoFileMetaData mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepoFileMetaData entity = new RepoFileMetaData();
            entity.setStoragePoolId(Guid.createGuidFromString(rs.getString("storage_pool_id")));
            entity.setRepoDomainId(Guid.createGuidFromString(rs.getString("storage_domain_id")));
            entity.setRepoFileName(rs.getString("repo_file_name"));
            entity.setStoragePoolStatus(StoragePoolStatus.forValue(rs.getInt("storage_pool_status")));
            entity.setVdsStatus(VDSStatus.forValue(rs.getInt("vds_status")));
            entity.setSize(rs.getLong("size"));
            entity.setStorageDomainStatus(StorageDomainStatus.forValue(rs.getInt("storage_domain_status")));
            entity.setDateCreated((Date) rs.getObject("date_created"));
            entity.setLastRefreshed(rs.getLong("last_refreshed"));
            return entity;
        }
    }
}
