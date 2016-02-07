package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageRegisterVmListModel extends StorageRegisterEntityListModel<VM> {

    public StorageRegisterVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmImportTitle());
        setHelpTag(HelpTag.vm_register);
        setHashName("vm_register"); //$NON-NLS-1$
    }

    @Override
    RegisterEntityModel createRegisterEntityModel() {
        RegisterVmModel model = new RegisterVmModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        model.setHelpTag(HelpTag.register_virtual_machine);
        model.setHashName("register_virtual_machine"); //$NON-NLS-1$

        return model;
    }

    @Override
    ImportEntityData<VM> createImportEntityData(VM entity) {
        return new ImportVmData(entity);
    }

    @Override
    protected void syncSearch() {
        syncSearch(VdcQueryType.GetUnregisteredVms, new LexoNumericNameableComparator<>());
    }

    @Override
    protected String getListName() {
        return "StorageRegisterVmListModel"; //$NON-NLS-1$
    }
}
