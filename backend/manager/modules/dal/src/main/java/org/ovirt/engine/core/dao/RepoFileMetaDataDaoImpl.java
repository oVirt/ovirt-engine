package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>StorageDomainDaoImpl</code> provides an implementation of {@link StorageDomainDao} based on code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}. Responsible for managing the repository files meta data via the
 * persistent layer.
 */
@Named
@Singleton
public class RepoFileMetaDataDaoImpl extends BaseDao implements RepoFileMetaDataDao {

    @Override
    public void removeRepoDomainFileList(Guid id, ImageFileType fileType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        if (fileType == null || fileType == ImageFileType.All) {
            parameterSource.addValue("file_type", null);
        } else {
            parameterSource.addValue("file_type", fileType.getValue());
        }

        getCallsHandler().executeModification("DeleteRepo_domain_file_list", parameterSource);
    }

    @Override
    public void addRepoFileMap(RepoImage map) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("repo_domain_id", map.getRepoDomainId())
                        .addValue("repo_image_id", map.getRepoImageId())
                        .addValue("repo_image_name", map.getRepoImageName())
                        .addValue("size", map.getSize())
                        .addValue("date_created", map.getDateCreated())
                        .addValue("last_refreshed", map.getLastRefreshed())
                        .addValue("file_type", map.getFileType().getValue());

        getCallsHandler().executeModification("InsertRepo_domain_file_meta_data", parameterSource);
    }

    /**
     * Returns a list of repository files with specific file extension from storage domain id.<BR/>
     * If no repository is found, the method will return an empty list.
     */
    @Override
    public List<RepoImage> getRepoListForStorageDomain(Guid storageDomainId,
            ImageFileType fileType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", storageDomainId);

        if (fileType == null || fileType == ImageFileType.All) {
            parameterSource.addValue("file_type", null);
        } else {
            parameterSource.addValue("file_type", fileType.getValue());
        }

        return getCallsHandler().executeReadList("GetRepo_files_by_storage_domain",
                RepoImageMapper.instance, parameterSource);
    }

    /**
     * Returns a list of all repository files with specific file extension from all the storage pools,
     * which meet the same status, type and SPM status.
     */
    @Override
    public List<RepoImage> getAllRepoFilesForAllStoragePools(StorageDomainType storageDomainType,
            StoragePoolStatus storagePoolStatus, StorageDomainStatus storageDomainStatus,
            VDSStatus vdsStatus) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("storage_domain_type", storageDomainType.getValue());
        parameterSource.addValue("storage_pool_status", storagePoolStatus.getValue());
        parameterSource.addValue("vds_status", vdsStatus.getValue());
        parameterSource.addValue("storage_domain_status", storageDomainStatus.getValue());

        return getCallsHandler().executeReadList("GetRepo_files_in_all_storage_pools",
                ThinRepoImageMapper.instance,
                parameterSource);
    }

    private static class ThinRepoImageMapper implements RowMapper<RepoImage> {
        public static final ThinRepoImageMapper instance = new ThinRepoImageMapper();

        @Override
        public RepoImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepoImage entity = new RepoImage();
            entity.setRepoDomainId(getGuidDefaultEmpty(rs, "storage_domain_id"));
            entity.setLastRefreshed(rs.getLong("last_refreshed"));
            entity.setFileType(ImageFileType.forValue(rs.getInt("file_type")));
            return entity;
        }
    }

    private static class RepoImageMapper implements RowMapper<RepoImage> {
        public static final RepoImageMapper instance = new RepoImageMapper();

        @Override
        public RepoImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepoImage entity = new RepoImage();
            entity.setRepoDomainId(getGuidDefaultEmpty(rs, "repo_domain_id"));
            entity.setRepoImageId(rs.getString("repo_image_id"));
            entity.setRepoImageName(rs.getString("repo_image_name"));
            entity.setSize((Long) rs.getObject("size"));
            entity.setDateCreated((Date) rs.getObject("date_created"));
            entity.setLastRefreshed(rs.getLong("last_refreshed"));
            entity.setFileType(ImageFileType.forValue(rs.getInt("file_type")));
            return entity;
        }
    }
}
