package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class SnapshotDetailModel extends EntityModel
{
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name"));
        }
    }

    private Snapshot snapshot;

    public Snapshot getSnapshot()
    {
        return snapshot;
    }

    public void setSnapshot(Snapshot value)
    {
        if (snapshot != value)
        {
            snapshot = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Snapshot"));
        }
    }

    public SnapshotDetailModel()
    {
        setName("");
    }

    public ListModel getEntityListModel() {
        ListModel listModel = new ListModel();
        ArrayList arrayList = new ArrayList();

        for (Object object : (List) getEntity()) {
            EntityModel entityModel = new EntityModel();
            entityModel.setEntity(object);

            arrayList.add(entityModel);
        }
        listModel.setItems(arrayList);

        return listModel;
    }
}
