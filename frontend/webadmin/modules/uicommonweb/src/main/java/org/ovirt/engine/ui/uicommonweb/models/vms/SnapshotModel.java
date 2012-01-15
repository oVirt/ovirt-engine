package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class SnapshotModel extends Model
{

    private Guid privateSnapshotId = new Guid();

    public Guid getSnapshotId()
    {
        return privateSnapshotId;
    }

    public void setSnapshotId(Guid value)
    {
        privateSnapshotId = value;
    }

    private boolean isPreviewed;

    public boolean getIsPreviewed()
    {
        return isPreviewed;
    }

    public void setIsPreviewed(boolean value)
    {
        if (isPreviewed != value)
        {
            isPreviewed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NAME"));
        }
    }

    private boolean isCurrent;

    public boolean getIsCurrent()
    {
        return isCurrent;
    }

    public void setIsCurrent(boolean value)
    {
        if (isCurrent != value)
        {
            isCurrent = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NAME"));
        }
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    /**
     * DescriptionValue: A simple getter, for use in the web GUI (it is impossible to bind values with type FieldModel
     * to an ext:Store).
     */
    public String getDescriptionValue()
    {
        return getDescription() == null ? null : (String) (getDescription().getEntity());
    }

    private java.util.Date date;

    public java.util.Date getDate()
    {
        return date;
    }

    public void setDate(java.util.Date value)
    {
        if (date == null || !date.equals(value))
        {
            date = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Date"));
        }
    }

    private String participantDisks;

    public String getParticipantDisks()
    {
        return participantDisks;
    }

    public void setParticipantDisks(String value)
    {
        if (!StringHelper.stringsEqual(participantDisks, value))
        {
            participantDisks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ParticipantDisks"));
        }
    }

    private java.util.List<EntityModel> disks;

    public java.util.List<EntityModel> getDisks()
    {
        return disks;
    }

    public void setDisks(java.util.List<EntityModel> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks"));
        }
    }

    private String apps;

    public String getApps()
    {
        return apps;
    }

    public void setApps(String value)
    {
        if (!StringHelper.stringsEqual(apps, value))
        {
            apps = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Apps"));
        }
    }

    public SnapshotModel()
    {
        setDescription(new EntityModel());
    }

    public boolean Validate()
    {
        getDescription().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

        boolean isDisksValid = false;
        setMessage(null);
        if (getDisks() != null)
        {
            for (EntityModel a : getDisks())
            {
                if (a.getIsSelected())
                {
                    isDisksValid = true;
                    break;
                }
            }
            if (!isDisksValid)
            {
                setMessage("At least one disk must be marked.");
                return false;
            }

            return getDescription().getIsValid();
        }

        return getDescription().getIsValid();
    }
}
