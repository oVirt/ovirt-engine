package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$)
            onRemove();
        }
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }
        model.startProgress();

        List<ActionParametersBase> vmTemplateImportExportParams = getSelectedItems()
                .stream()
                .map(item -> new VmTemplateImportExportParameters(item.getId(),
                        getEntity().getId(),
                        getEntity().getStoragePoolId()))
                .collect(Collectors.toList());

        Frontend.getInstance().runMultipleAction(
                ActionType.RemoveUnregisteredVmTemplate,
                vmTemplateImportExportParams,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                },
                model);
    }
}
