package org.ovirt.engine.ui.uicommonweb.models.disks;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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
            OnPropertyChanged(new PropertyChangedEventArgs("Alias")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("Storage Domain")); //$NON-NLS-1$
        }
    }

    private VolumeFormat privateVolumeFormat;

    public VolumeFormat getVolumeFormat()
    {
        return privateVolumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value)
    {
        if (privateVolumeFormat != value)
        {
            privateVolumeFormat = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Volume Format")); //$NON-NLS-1$
        }
    }

    private String diskId;

    public String getDiskId()
    {
        return diskId;
    }

    public void setDiskId(String value)
    {
        if (diskId != value)
        {
            diskId = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ID")); //$NON-NLS-1$
        }
    }

    private String privateQuotaName;

    public String getQuotaName()
    {
        return privateQuotaName;
    }

    public void setQuotaName(String value)
    {
        if (privateQuotaName != value)
        {
            privateQuotaName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Quota Name")); //$NON-NLS-1$
        }
    }

    public DiskGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (super.getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties()
    {
        DiskImage disk = (DiskImage) getEntity();

        setAlias(disk.getDiskAlias());
        setDescription(disk.getDiskDescription());
        setVolumeFormat(disk.getvolume_format());
        setDiskId(disk.getImageId().toString());
        setQuotaName(disk.getQuotaName());
    }
}
