package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ImportCloneDialogPopupView extends AbstractModelBoundPopupView<ImportCloneModel> implements ImportCloneDialogPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ImportCloneModel, ImportCloneDialogPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportCloneDialogPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @UiField
    @Path(value = "name.entity")
    StringEntityModelTextBoxEditor nameEditor;
    @UiField
    @Path(value = "suffix.entity")
    StringEntityModelTextBoxEditor suffixEditor;

    @UiField(provided = true)
    @Path(value = "clone.entity")
    EntityModelRadioButtonEditor cloneEditor;

    @UiField(provided = true)
    @Path(value = "noClone.entity")
    EntityModelRadioButtonEditor noCloneEditor;

    @UiField
    @Ignore
    Label dialogLabelEditor;

    @UiField
    @Ignore
    Label selectLabelEditor;

    @UiField(provided = true)
    @Path(value = "applyToAll.entity")
    EntityModelCheckBoxEditor applyToAllEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ImportCloneDialogPopupView(EventBus eventBus) {
        super(eventBus);
        initSelectWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    private void initSelectWidgets() {
        applyToAllEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cloneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        noCloneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
    }

    private void localize() {
        nameEditor.setLabel(constants.import_newName());
        selectLabelEditor.setText(constants.cloneSelect());
        applyToAllEditor.setLabel(constants.cloneApplyToAll());
        noCloneEditor.setLabel(constants.cloneDontImport());
    }

    @Override
    public void edit(ImportCloneModel object) {
        if (((ImportEntityData<?>) object.getEntity()).getEntity() instanceof VM) {
            dialogLabelEditor.setText(messages.sameVmNameExists(((ImportVmData) object.getEntity()).getVm().getName()));
            cloneEditor.setLabel(constants.cloneImportVmDetails());
            suffixEditor.setLabel(constants.cloneImportSuffixVm());
        } else {
            dialogLabelEditor.setText(constants.sameTemplateNameExists()
                    + " (" + ((ImportTemplateData) object.getEntity()).getTemplate().getName() + ")");//$NON-NLS-1$ //$NON-NLS-2$
            cloneEditor.setLabel(constants.cloneImportTemplate());
            suffixEditor.setLabel(constants.cloneImportSuffixTemplate());
        }
        driver.edit(object);
    }

    @Override
    public ImportCloneModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
