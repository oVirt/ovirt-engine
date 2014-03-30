package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ConfirmationModel extends ListModel
{

    private EntityModel<Boolean> privateLatch;

    public EntityModel<Boolean> getLatch()
    {
        return privateLatch;
    }

    public void setLatch(EntityModel<Boolean> value)
    {
        privateLatch = value;
    }

    private EntityModel<Boolean> force;

    public EntityModel<Boolean> getForce()
    {
        return force;
    }

    public void setForce(EntityModel<Boolean> value)
    {
        force = value;
    }

    private String forceLabel;

    public String getForceLabel() {
        return forceLabel;
    }

    public void setForceLabel(String forceLabel) {
        if (!ObjectUtils.objectsEqual(getForceLabel(), forceLabel))
        {
            this.forceLabel = forceLabel;
            onPropertyChanged(new PropertyChangedEventArgs("ForceLabel")); //$NON-NLS-1$
        }
    }

    private String note;

    public String getNote()
    {
        return note;
    }

    public void setNote(String value)
    {
        if (!ObjectUtils.objectsEqual(note, value))
        {
            note = value;
            onPropertyChanged(new PropertyChangedEventArgs("Note")); //$NON-NLS-1$
        }
    }

    public ConfirmationModel()
    {
        setLatch(new EntityModel<Boolean>());
        getLatch().setEntity(false);
        getLatch().setIsAvailable(false);

        setForce(new EntityModel<Boolean>());
        getForce().setEntity(false);
        getForce().setIsAvailable(false);
    }

    public boolean validate()
    {
        getLatch().setIsValid(true);
        if (getLatch().getIsAvailable() && !getLatch().getEntity())
        {
            getLatch().getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .youMustApproveTheActionByClickingOnThisCheckboxInvalidReason());
            getLatch().setIsValid(false);
        }

        return getLatch().getIsValid();
    }
}
