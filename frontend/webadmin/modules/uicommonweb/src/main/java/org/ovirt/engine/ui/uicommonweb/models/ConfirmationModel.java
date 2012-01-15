package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;

@SuppressWarnings("unused")
public class ConfirmationModel extends ListModel
{

    private EntityModel privateLatch;

    public EntityModel getLatch()
    {
        return privateLatch;
    }

    private void setLatch(EntityModel value)
    {
        privateLatch = value;
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
            OnPropertyChanged(new PropertyChangedEventArgs("Note"));
        }
    }

    public ConfirmationModel()
    {
        setLatch(new EntityModel());
        getLatch().setEntity(false);
        getLatch().setIsAvailable(false);
    }

    public boolean Validate()
    {
        getLatch().setIsValid(true);
        if (getLatch().getIsAvailable() && !(Boolean) getLatch().getEntity())
        {
            getLatch().getInvalidityReasons().add("You must approve the action by clicking on this checkbox.");
            getLatch().setIsValid(false);
        }

        return getLatch().getIsValid();
    }
}
