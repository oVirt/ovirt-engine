package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;

/**
 * An entity class for repository files meta data. Using for caching VDSM list fetching results.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RepoFileMetaData")
public class RepoFileMetaData extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
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
    @XmlElement(name = "StoragePoolId")
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
    @XmlElement(name = "StoragePoolStatus")
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
    @XmlElement(name = "VdsStatus")
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
    @XmlElement(name = "RepoDomainId")
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
    @XmlElement(name = "StorageDomainStatus")
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
    @XmlElement(name = "RepoFileName")
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
    @XmlElement(name = "Size")
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
    @XmlElement(name = "DateCreated")
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
    @XmlElement(name = "lastRefreshed")
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
    @XmlElement(name = "fileType")
    public FileTypeExtension getFileType() {
        return fileType;
    }
}
