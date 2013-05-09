package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class RemoveStorageModel extends Model
{

    private ListModel privateHostList;

    public ListModel getHostList()
    {
        return privateHostList;
    }

    private void setHostList(ListModel value)
    {
        privateHostList = value;
    }

    private EntityModel privateFormat;

    public EntityModel getFormat()
    {
        return privateFormat;
    }

    private void setFormat(EntityModel value)
    {
        privateFormat = value;
    }

    public RemoveStorageModel()
    {
        setHostList(new ListModel());

        setFormat(new EntityModel());
        getFormat().setEntity(false);
    }

    public boolean validate()
    {
        getHostList().setIsValid(getHostList().getSelectedItem() != null);

        return getHostList().getIsValid();
    }
}
