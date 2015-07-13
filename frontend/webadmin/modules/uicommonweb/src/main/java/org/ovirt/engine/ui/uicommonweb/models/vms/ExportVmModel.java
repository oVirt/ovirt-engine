package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ExportVmModel extends Model {
    private ListModel<StorageDomain> privateStorage;

    public ListModel<StorageDomain> getStorage() {
        return privateStorage;
    }

    private void setStorage(ListModel<StorageDomain> value) {
        privateStorage = value;
    }

    private EntityModel<Boolean> privateCollapseSnapshots;

    public EntityModel<Boolean> getCollapseSnapshots() {
        return privateCollapseSnapshots;
    }

    private void setCollapseSnapshots(EntityModel<Boolean> value) {
        privateCollapseSnapshots = value;
    }

    private EntityModel<Boolean> privateForceOverride;

    public EntityModel<Boolean> getForceOverride() {
        return privateForceOverride;
    }

    private void setForceOverride(EntityModel<Boolean> value) {
        privateForceOverride = value;
    }

    public ExportVmModel() {
        setStorage(new ListModel<StorageDomain>());

        setCollapseSnapshots(new EntityModel<Boolean>());
        getCollapseSnapshots().setEntity(false);

        setForceOverride(new EntityModel<Boolean>());
        getForceOverride().setEntity(false);
    }

    public boolean validate() {
        getStorage().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getStorage().getIsValid();
    }
}
