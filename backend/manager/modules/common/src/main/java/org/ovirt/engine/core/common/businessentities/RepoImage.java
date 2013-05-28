package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.compat.Guid;

/**
 * An entity class for repository files meta data. Using for caching VDSM list fetching results.
 */
public class RepoImage extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = 566928138057530047L;
    private Guid storagePoolId = Guid.Empty;
    private StoragePoolStatus storagePoolStatus;
    private VDSStatus vdsStatus;
    private Guid repoDomainId = Guid.Empty;
    private StorageDomainStatus storageDomainStatus;
    private String repoImageId;
    private String repoImageName;
    private long size = 0;
    private Date dateCreated = new Date();
    private long lastRefreshed;
    private ImageFileType fileType;

    /**
     * Empty constructor for retrieving new clean entity
     */
    public RepoImage() {
    }

    /**
     * @param storagePoolId
     *            the storage pool id to set
     */
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    /**
     * @return the storagePoolId
     */
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    /**
     * @param storagePoolStatus
     *            the storage pool id to set
     */
    public void setStoragePoolStatus(StoragePoolStatus storagePoolStatus) {
        this.storagePoolStatus = storagePoolStatus;
    }

    /**
     * @return the storagePoolStatus
     */
    public StoragePoolStatus getStoragePoolStatus() {
        return storagePoolStatus;
    }

    /**
     * @param vdsStatus
     *            the vds status to set
     */
    public void setVdsStatus(VDSStatus vdsStatus) {
        this.vdsStatus = vdsStatus;
    }

    /**
     * @return the vds status.
     */
    public VDSStatus getVdsStatus() {
        return vdsStatus;
    }

    /**
     * @param repoDomainId
     *            the repository domain Id to set.
     */
    public void setRepoDomainId(Guid repoDomainId) {
        this.repoDomainId = repoDomainId;
    }

    /**
     * @return the repository domain Id.
     */
    public Guid getRepoDomainId() {
        return repoDomainId;
    }

    /**
     * @param storageDomainStatus
     *            the storage domain status to set
     */
    public void setStorageDomainStatus(StorageDomainStatus storageDomainStatus) {
        this.storageDomainStatus = storageDomainStatus;
    }

    /**
     * @return the storage domain status.
     */
    public StorageDomainStatus getStorageDomainStatus() {
        return storageDomainStatus;
    }

    /**
     * @param repoImageId
     *            the repository image id to set
     */
    public void setRepoImageId(String repoImageId) {
        this.repoImageId = repoImageId;
    }

    /**
     * @return the repository image id
     */
    public String getRepoImageId() {
        return repoImageId;
    }

    @Override
    public Object getQueryableId() {
        return getRepoImageId();
    }

    /**
     * @param repoImageName
     *            the repository image name to set
     */
    public void setRepoImageName(String repoImageName) {
        this.repoImageName = repoImageName;
    }

    /**
     * @return the repository image name
     */
    public String getRepoImageName() {
        return repoImageName;
    }

    /**
     * @return the repository image title to be displayed
     */
    public String getRepoImageTitle() {
        if (repoImageName != null) {
            // To provide an hint about the image id and at the same time
            // maintain the image title short we just report 7 characters
            // of the id (similarly to what git does with hashes).
            return repoImageName + " (" + repoImageId.substring(0, 7) + ")";
        } else {
            return repoImageId;
        }
    }

    /**
     * @param size
     *            the size to set For future use.
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the size For future use.
     */
    public long getSize() {
        return size;
    }

    /**
     * @param dateCreated
     *            the date the file created to set For future use.
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the dateCreated For future use.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param lastRefreshed
     *            the system time the file was last refreshed from VDSM.
     */
    public void setLastRefreshed(long lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    /**
     * @return The last refreshed time of the file repository.
     */
    public long getLastRefreshed() {
        return lastRefreshed;
    }

    /**
     * @param fileType
     *            - The file type extension.
     */
    public void setFileType(ImageFileType fileType) {
        this.fileType = fileType;
    }

    /**
     * @return the file type.
     */
    public ImageFileType getFileType() {
        return fileType;
    }
}
