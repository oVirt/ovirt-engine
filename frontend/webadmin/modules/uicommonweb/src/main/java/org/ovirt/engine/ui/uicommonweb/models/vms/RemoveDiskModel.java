package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

@SuppressWarnings("unused")
public class RemoveDiskModel extends ConfirmationModel
{
    public RemoveDiskModel()
    {
        setLatch(new EntityModel());
        getLatch().setIsAvailable(true);
    }

    @Override
    public boolean validate()
    {
        return true;
    }

}
