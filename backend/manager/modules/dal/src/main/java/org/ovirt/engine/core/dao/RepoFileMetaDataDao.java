package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code RepoFileMetaDataDao} defines a type for performing CRUD operations on instances of {@link RepoImage}.
 *
 */
public interface RepoFileMetaDataDao extends Dao {
    /**
     * Remove repository file list from cache table, of domain with the specified id.
     *
     * @param id - The domain id.
     * @param filetype - The file Extension, which should be removed.
     */
    void removeRepoDomainFileList(Guid id, ImageFileType filetype);

    /**
     * Add repository file to cache table.
     *
     * @param map - The repository file meta data to insert.
     */
    void addRepoFileMap(RepoImage map);

    /**
     * Returns a list of repository files with specific file extension from storage domain id.<BR/>
     * If no repository found, will return an empty list.
     */
    List<RepoImage> getRepoListForStorageDomain(Guid storageDomainId,
            ImageFileType fileType);
}
