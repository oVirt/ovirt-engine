package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageRegisterVmListModel extends StorageRegisterEntityListModel<VM, RegisterVmData> {

    public StorageRegisterVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmImportTitle());
        setHelpTag(HelpTag.vm_register);
        setHashName("vm_register"); //$NON-NLS-1$
    }

    @Override
    RegisterEntityModel<VM, RegisterVmData> createRegisterEntityModel() {
        RegisterVmModel model = new RegisterVmModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        model.setHelpTag(HelpTag.register_virtual_machine);
        model.setHashName("register_virtual_machine"); //$NON-NLS-1$

        return model;
    }

    @Override
    ConfirmationModel createRemoveEntityModel() {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().removeUnregisteredVirtualMachineTitle());
        confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().removeConfirmationPopupMessage());
        confirmationModel.setHelpTag(HelpTag.remove_unregistered_virtual_machine);
        confirmationModel.setHashName("remove_unregistered_vms"); //$NON-NLS-1$
        return confirmationModel;
    }

    @Override
    List<String> getSelectedItemsNames() {
        return getSelectedItems().stream()
                .filter(vm -> !Guid.isNullOrEmpty(vm.getId()))
                .map(VM::getName)
                .collect(Collectors.toList());
    }

    @Override
    List<ActionParametersBase> getRemoveUnregisteredEntityParams(Guid storagePoolId) {
        return getSelectedItems()
                .stream()
                .map(item -> new RemoveUnregisteredEntityParameters(
                        item.getId(),
                        getEntity().getId(),
                        storagePoolId))
                .collect(Collectors.toList());
    }

    @Override
    ActionType getRemoveActionType() {
        return ActionType.RemoveUnregisteredVm;
    }

    @Override
    RegisterVmData createImportEntityData(VM entity) {
        return new RegisterVmData(entity);
    }

    @Override
    protected void syncSearch() {
        syncSearch(QueryType.GetUnregisteredVms, new LexoNumericNameableComparator<>());
    }

    @Override
    protected String getListName() {
        return "StorageRegisterVmListModel"; //$NON-NLS-1$
    }
}
