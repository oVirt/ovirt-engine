package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class StorageGeneralModel extends EntityModel<StorageDomain> {

    private boolean isNfs;

    public boolean getIsNfs() {
        return isNfs;
    }

    public void setIsNfs(boolean value) {
        if (isNfs != value) {
            isNfs = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsNfs")); //$NON-NLS-1$
        }
    }

    private boolean isLocalS;

    public boolean getIsLocalS() {
        return isLocalS;
    }

    public void setIsLocalS(boolean value) {
        if (isLocalS != value) {
            isLocalS = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsLocalS")); //$NON-NLS-1$
        }
    }

    private boolean isPosix;

    public boolean getIsPosix() {
        return isPosix;
    }

    public void setIsPosix(boolean value) {
        if (isPosix != value) {
            isPosix = value;
            onPropertyChanged(new PropertyChangedEventArgs("isPosix")); //$NON-NLS-1$
        }
    }

    private boolean isGlusterfs;

    public boolean getIsGlusterfs() {
        return isGlusterfs;
    }

    public void setIsGlusterfs(boolean value) {
        if (isGlusterfs != value) {
            isGlusterfs = value;
            onPropertyChanged(new PropertyChangedEventArgs("isGlusterfs")); //$NON-NLS-1$
        }
    }

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        if (!Objects.equals(path, value)) {
            path = value;
            onPropertyChanged(new PropertyChangedEventArgs("Path")); //$NON-NLS-1$
        }
    }

    private String vfsType;

    public String getVfsType() {
        return vfsType;
    }

    public void setVfsType(String vfsType) {
        if (!Objects.equals(this.vfsType, vfsType)) {
            this.vfsType = vfsType;
            onPropertyChanged(new PropertyChangedEventArgs("VfsType")); //$NON-NLS-1$
        }
    }

    private String mountOptions;

    public String getMountOptions() {
        return mountOptions;
    }

    public void setMountOptions(String mountOptions) {
        if (!Objects.equals(this.mountOptions, mountOptions)) {
            this.mountOptions = mountOptions;
            onPropertyChanged(new PropertyChangedEventArgs("MountOptions")); //$NON-NLS-1$
        }
    }

    String nfsVersion;

    public String getNfsVersion() {
        return nfsVersion;
    }

    public void setNfsVersion(String nfsVersion) {
        if (!Objects.equals(this.nfsVersion, nfsVersion)) {
            this.nfsVersion = nfsVersion;
            onPropertyChanged(new PropertyChangedEventArgs("NfsVersion")); //$NON-NLS-1$
        }
    }

    Short timeout;

    public String getTimeout() {
        return timeout == null ? null : timeout.toString();
    }

    public void setTimeout(Short timeout) {
        if (!isShortsEqual(this.timeout, timeout)) {
            this.timeout = timeout;
            onPropertyChanged(new PropertyChangedEventArgs("Timeout")); //$NON-NLS-1$
        }
    }

    Short retransmissions;

    public String getRetransmissions() {
        return retransmissions == null ? null : retransmissions.toString();
    }

    public void setRetransmissions(Short retransmissions) {
        if (!isShortsEqual(this.retransmissions, retransmissions)) {
            this.retransmissions = retransmissions;
            onPropertyChanged(new PropertyChangedEventArgs("Retransmissions")); //$NON-NLS-1$
        }
    }

    private String numOfImages;

    public String getNumOfImages() {
        return numOfImages;
    }

    public void setNumOfImages(String numOfImages) {
        if (!Objects.equals(this.numOfImages, numOfImages)) {
            this.numOfImages = numOfImages;
            onPropertyChanged(new PropertyChangedEventArgs("NumOfImages")); //$NON-NLS-1$
        }
    }

    public StorageGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            StorageDomain storageDomain = getEntity();

            setIsNfs(storageDomain.getStorageType() == StorageType.NFS);
            setIsLocalS(storageDomain.getStorageType() == StorageType.LOCALFS);
            setIsPosix(storageDomain.getStorageType() == StorageType.POSIXFS);
            setIsGlusterfs(storageDomain.getStorageType() == StorageType.GLUSTERFS);

            if (getEntity().getStorageDomainType().isDataDomain()) {
                AsyncDataProvider.getInstance().getNumberOfImagesOnStorageDomain(
                        new AsyncQuery<>(num -> setNumOfImages(num.toString())), storageDomain.getId());
            }

            if (getIsNfs() || getIsLocalS() || getIsPosix() || getIsGlusterfs()) {
                AsyncDataProvider.getInstance().getStorageConnectionById(new AsyncQuery<>(connection -> {
                    if (connection != null) {
                        setPath(connection.getConnection());
                        if (isNfs) {
                            EnumTranslator translator = EnumTranslator.getInstance();
                            setNfsVersion(translator.translate(connection.getNfsVersion()));
                            setRetransmissions(connection.getNfsRetrans());
                            setTimeout(connection.getNfsTimeo());
                        }

                        if (isPosix || isGlusterfs) {
                            setVfsType(connection.getVfsType());
                            setMountOptions(connection.getMountOptions());
                        }
                    } else {
                        setPath(null);
                    }
                }), storageDomain.getStorage(), false);
            } else {
                setPath(null);
            }

        }
    }

    private boolean isShortsEqual(Short a, Short b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
