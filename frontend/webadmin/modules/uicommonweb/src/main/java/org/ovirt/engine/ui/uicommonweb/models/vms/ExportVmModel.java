package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ExportVmModel extends Model
{
    private ListModel privateStorage;

    public ListModel getStorage()
    {
        return privateStorage;
    }

    private void setStorage(ListModel value)
    {
        privateStorage = value;
    }

    private EntityModel privateCollapseSnapshots;

    public EntityModel getCollapseSnapshots()
    {
        return privateCollapseSnapshots;
    }

    private void setCollapseSnapshots(EntityModel value)
    {
        privateCollapseSnapshots = value;
    }

    private EntityModel privateForceOverride;

    public EntityModel getForceOverride()
    {
        return privateForceOverride;
    }

    private void setForceOverride(EntityModel value)
    {
        privateForceOverride = value;
    }

    public ExportVmModel()
    {
        setStorage(new ListModel());

        setCollapseSnapshots(new EntityModel());
        getCollapseSnapshots().setEntity(false);

        setForceOverride(new EntityModel());
        getForceOverride().setEntity(false);
    }

    public boolean Validate()
    {
        getStorage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getStorage().getIsValid();
    }
}
