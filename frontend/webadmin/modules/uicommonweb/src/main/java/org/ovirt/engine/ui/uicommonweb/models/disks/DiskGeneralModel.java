package org.ovirt.engine.ui.uicommonweb.models.disks;

import com.google.gwt.i18n.client.DateTimeFormat;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DiskGeneralModel extends EntityModel
{
    private String privateAlias;

    public String getAlias()
    {
        return privateAlias;
    }

    public void setAlias(String value)
    {
        if (!StringHelper.stringsEqual(privateAlias, value))
        {
            privateAlias = value;
            onPropertyChanged(new PropertyChangedEventArgs("Alias")); //$NON-NLS-1$
        }
    }

    private String privateDescription;

    public String getDescription()
    {
        return privateDescription;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(privateDescription, value))
        {
            privateDescription = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private String privateStorageDomain;

    public String getStorageDomain()
    {
        return privateStorageDomain;
    }

    public void setStorageDomain(String value)
    {
        if (!StringHelper.stringsEqual(privateStorageDomain, value))
        {
            privateStorageDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Storage Domain")); //$NON-NLS-1$
        }
    }

    private String diskId;

    public String getDiskId()
    {
        return diskId;
    }

    public void setDiskId(String value)
    {
        if (!StringHelper.stringsEqual(diskId, value))
        {
            diskId = value;
            onPropertyChanged(new PropertyChangedEventArgs("ID")); //$NON-NLS-1$
        }
    }

    private String privateLunId;

    public String getLunId()
    {
        return privateLunId;
    }

    public void setLunId(String value)
    {
        if (!StringHelper.stringsEqual(privateLunId, value))
        {
            privateLunId = value;
            onPropertyChanged(new PropertyChangedEventArgs("LUN ID")); //$NON-NLS-1$
        }
    }

    private String privateAlignment;

    public String getAlignment()
    {
        return privateAlignment;
    }

    public void setAlignment(String value)
    {
        if (privateAlignment != value) {
            privateAlignment = value;
            onPropertyChanged(new PropertyChangedEventArgs("Alignment")); //$NON-NLS-1$
        }
    }

    private String privateQuotaName;

    public String getQuotaName()
    {
        return privateQuotaName;
    }

    public void setQuotaName(String value)
    {
        if (!StringHelper.stringsEqual(privateQuotaName, value))
        {
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

    public DiskGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (super.getEntity() != null)
        {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties()
    {
        Disk disk = (Disk) getEntity();

        setImage(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        setLun(disk.getDiskStorageType() == DiskStorageType.LUN);

        setAlias(disk.getDiskAlias());
        setDescription(disk.getDiskDescription());
        setDiskId(disk.getId().toString());

        if (disk.getLastAlignmentScan() != null) {
            String lastScanDate = DateTimeFormat
                    .getFormat("yyyy-MM-dd, HH:mm").format(disk.getLastAlignmentScan()); //$NON-NLS-1$
            setAlignment(ConstantsManager.getInstance()
                    .getMessages().diskAlignment(disk.getAlignment().toString(), lastScanDate));
        } else {
            setAlignment(disk.getAlignment().toString());
        }

        if (isImage()) {
            DiskImage diskImage = (DiskImage) disk;
            setQuotaName(diskImage.getQuotaName());
            setQuotaAvailable(!diskImage.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));
        }
        else if (isLun()) {
            LunDisk lunDisk = (LunDisk) disk;
            setLunId(lunDisk.getLun().getLUN_id());
        }
    }
}
