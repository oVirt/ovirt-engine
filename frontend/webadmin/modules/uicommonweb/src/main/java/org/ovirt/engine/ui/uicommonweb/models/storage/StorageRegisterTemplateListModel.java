package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageRegisterTemplateListModel extends StorageRegisterEntityListModel<VmTemplate, ImportTemplateData> {

    public StorageRegisterTemplateListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().templateImportTitle());
        setHelpTag(HelpTag.template_register);
        setHashName("template_register"); //$NON-NLS-1$
    }

    @Override
    RegisterEntityModel<VmTemplate, ImportTemplateData> createRegisterEntityModel() {
        RegisterTemplateModel model = new RegisterTemplateModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importTemplatesTitle());
        model.setHelpTag(HelpTag.register_template);
        model.setHashName("register_template"); //$NON-NLS-1$

        return model;
    }

    @Override
    ImportTemplateData createImportEntityData(VmTemplate entity) {
        return new ImportTemplateData(entity);
    }

    @Override
    protected void syncSearch() {
        syncSearch(QueryType.GetUnregisteredVmTemplates, new LexoNumericNameableComparator<>());
    }

    @Override
    protected String getListName() {
        return "StorageRegisterTemplateListModel"; //$NON-NLS-1$
    }

    @Override
    ConfirmationModel createRemoveEntityModel() {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().removeUnregisteredTemplatesTitle());
        confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().removeConfirmationPopupMessage());
        confirmationModel.setHelpTag(HelpTag.remove_unregistered_template);
        confirmationModel.setHashName("remove_unregistered_templates"); //$NON-NLS-1$
        return confirmationModel;
    }

    @Override
    List<String> getSelectedItemsNames() {
        return getSelectedItems().stream()
                .filter(vmTemplate -> !Guid.isNullOrEmpty(vmTemplate.getId()))
                .map(VmTemplate::getName)
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
        return ActionType.RemoveUnregisteredVmTemplate;
    }
}
