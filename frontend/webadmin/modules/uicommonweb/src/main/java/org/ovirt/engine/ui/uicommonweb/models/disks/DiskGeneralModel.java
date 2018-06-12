package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DiskGeneralModel extends EntityModel<Disk> {
    private String privateAlias;

    public String getAlias() {
        return privateAlias;
    }

    public void setAlias(String value) {
        if (!Objects.equals(privateAlias, value)) {
            privateAlias = value;
            onPropertyChanged(new PropertyChangedEventArgs("Alias")); //$NON-NLS-1$
        }
    }

    private String privateDescription;

    public String getDescription() {
        return privateDescription;
    }

    public void setDescription(String value) {
        if (!Objects.equals(privateDescription, value)) {
            privateDescription = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private String privateStorageDomain;

    public String getStorageDomain() {
        return privateStorageDomain;
    }

    public void setStorageDomain(String value) {
        if (!Objects.equals(privateStorageDomain, value)) {
            privateStorageDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Storage Domain")); //$NON-NLS-1$
        }
    }

    private String diskId;

    public String getDiskId() {
        return diskId;
    }

    public void setDiskId(String value) {
        if (!Objects.equals(diskId, value)) {
            diskId = value;
            onPropertyChanged(new PropertyChangedEventArgs("ID")); //$NON-NLS-1$
        }
    }

    private String privateLunId;

    public String getLunId() {
        return privateLunId;
    }

    public void setLunId(String value) {
        if (!Objects.equals(privateLunId, value)) {
            privateLunId = value;
            onPropertyChanged(new PropertyChangedEventArgs("LUN ID")); //$NON-NLS-1$
        }
    }

    private boolean wipeAfterDelete;

    public boolean getWipeAfterDelete() {
        return wipeAfterDelete;
    }

    public void setWipeAfterDelete(boolean wipeAfterDelete) {
        if (this.wipeAfterDelete != wipeAfterDelete) {
            this.wipeAfterDelete = wipeAfterDelete;
            onPropertyChanged(new PropertyChangedEventArgs("WipeAfterDelete")); //$NON-NLS-1$
        }
    }

    private long virtualSize;

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        if (this.virtualSize != virtualSize) {
            this.virtualSize = virtualSize;
            onPropertyChanged(new PropertyChangedEventArgs("virtualSize")); //$NON-NLS-1$
        }
    }

    private double actualSize;

    public double getActualSize() {
        return actualSize;
    }

    public void setActualSize(double actualSize) {
        if (this.actualSize != actualSize) {
            this.actualSize = actualSize;
            onPropertyChanged(new PropertyChangedEventArgs("actualSize")); //$NON-NLS-1$
        }
    }

    private String privateDiskProfileName;

    public String getDiskProfileName() {
        return privateDiskProfileName;
    }

    public void setDiskProfileName(String value) {
        if (!Objects.equals(privateDiskProfileName, value)) {
            privateDiskProfileName = value;
            onPropertyChanged(new PropertyChangedEventArgs("DiskProfile Name")); //$NON-NLS-1$
        }
    }

    private String privateQuotaName;

    public String getQuotaName() {
        return privateQuotaName;
    }

    public void setQuotaName(String value) {
        if (!Objects.equals(privateQuotaName, value)) {
            privateQuotaName = value;
            onPropertyChanged(new PropertyChangedEventArgs("Quota Name")); //$NON-NLS-1$
        }
    }

    private boolean quotaAvailable;

    public boolean isQuotaAvailable() {
        return quotaAvailable;
    }

    public void setQuotaAvailable(boolean quotaAvailable) {
        this.quotaAvailable = quotaAvailable;
    }

    private boolean image;

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    private boolean lun;

    public boolean isLun() {
        return lun;
    }

    public void setLun(boolean lun) {
        this.lun = lun;
    }

    public DiskGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (super.getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        Disk disk = getEntity();

        setImage(disk.getDiskStorageType().isInternal());
        setLun(disk.getDiskStorageType() == DiskStorageType.LUN);

        setAlias(disk.getDiskAlias());
        setDescription(disk.getDiskDescription());
        setDiskId(disk.getId().toString());
        setVirtualSize(disk.getSize());
        setWipeAfterDelete(disk.isWipeAfterDelete());

        if (isImage()) {
            DiskImage diskImage = (DiskImage) disk;
            setVirtualSize(diskImage.getSizeInGigabytes());
            setActualSize(diskImage.getActualSize());
            setDiskProfileName(StringHelper.nullSafeJoin(",", diskImage.getDiskProfileNames())); //$NON-NLS-1$
            setQuotaName(StringHelper.nullSafeJoin(",", diskImage.getQuotaNames())); //$NON-NLS-1$
            setQuotaAvailable(!diskImage.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));
        } else if (isLun()) {
            LunDisk lunDisk = (LunDisk) disk;
            setLunId(lunDisk.getLun().getLUNId());
        }
    }
}
