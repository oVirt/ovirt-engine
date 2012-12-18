package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class ConfirmationModel extends ListModel
{

    private EntityModel privateLatch;

    public EntityModel getLatch()
    {
        return privateLatch;
    }

    public void setLatch(EntityModel value)
    {
        privateLatch = value;
    }

    private EntityModel force;

    public EntityModel getForce()
    {
        return force;
    }

    public void setForce(EntityModel value)
    {
        force = value;
    }

    private String note;

    public String getNote()
    {
        return note;
    }

    public void setNote(String value)
    {
        if (!StringHelper.stringsEqual(note, value))
        {
            note = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Note")); //$NON-NLS-1$
        }
    }

    public ConfirmationModel()
    {
        setLatch(new EntityModel());
        getLatch().setEntity(false);
        getLatch().setIsAvailable(false);

        setForce(new EntityModel());
        getForce().setEntity(false);
        getForce().setIsAvailable(false);
    }

    public boolean Validate()
    {
        getLatch().setIsValid(true);
        if (getLatch().getIsAvailable() && !(Boolean) getLatch().getEntity())
        {
            getLatch().getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .youMustApproveTheActionByClickingOnThisCheckboxInvalidReason());
            getLatch().setIsValid(false);
        }

        return getLatch().getIsValid();
    }
}
