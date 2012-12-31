package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.compat.Guid;

/**
 * An entity class for repository files meta data. Using for caching VDSM list fetching results.
 */
public class RepoFileMetaData extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = 566928138057530047L;
    private Guid storagePoolId = new Guid();
    private StoragePoolStatus storagePoolStatus;
    private VDSStatus vdsStatus;
    private Guid repoDomainId = new Guid();
    private StorageDomainStatus storageDomainStatus;
    private String repoFileName;
    private long size = 0;
    private Date dateCreated = new Date();
    private long lastRefreshed;
    private FileTypeExtension fileType;

    /**
     * Empty constructor for retrieving new clean entity
     */
    public RepoFileMetaData() {
    }

    /**
     * @param StoragePoolId
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
     * @param repoFileName
     *            the repository file name to set
     */
    public void setRepoFileName(String repoFileName) {
        this.repoFileName = repoFileName;
    }

    /**
     * @return the repository file name.
     */
    public String getRepoFileName() {
        return repoFileName;
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
    public void setFileType(FileTypeExtension fileType) {
        this.fileType = fileType;
    }

    /**
     * @return the file type.
     */
    public FileTypeExtension getFileType() {
        return fileType;
    }
}
