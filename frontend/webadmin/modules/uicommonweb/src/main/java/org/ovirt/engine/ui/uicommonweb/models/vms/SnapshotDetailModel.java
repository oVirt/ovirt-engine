package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class SnapshotDetailModel extends EntityModel {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private Snapshot snapshot;

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot value) {
        if (snapshot != value) {
            snapshot = value;
            onPropertyChanged(new PropertyChangedEventArgs("Snapshot")); //$NON-NLS-1$
        }
    }

    public SnapshotDetailModel() {
        setName(""); //$NON-NLS-1$
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
