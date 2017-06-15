package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.LongEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.quota.EditQuotaStorageModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.EditQuotaStoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class EditQuotaStoragePopupView extends AbstractModelBoundPopupView<EditQuotaStorageModel> implements EditQuotaStoragePopupPresenterWidget.ViewDef {

    @UiField(provided = true)
    @Path(value = "unlimitedStorage.entity")
    @WithElementId
    EntityModelRadioButtonEditor unlimitedStorageRadioButtonEditor;

    @UiField(provided = true)
    @Path(value = "specificStorage.entity")
    @WithElementId
    EntityModelRadioButtonEditor specificStorageRadioButtonEditor;

    @UiField
    @Path(value = "specificStorageValue.entity")
    @WithElementId
    LongEntityModelTextBoxEditor storageValueEditor;

    @UiField
    @Ignore
    Label storageLabel;

    interface Driver extends UiCommonEditorDriver<EditQuotaStorageModel, EditQuotaStoragePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, EditQuotaStoragePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<EditQuotaStoragePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public EditQuotaStoragePopupView(EventBus eventBus) {
        super(eventBus);
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        storageValueEditor.hideLabel();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initRadioButtonEditors() {
        unlimitedStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
        specificStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
    }

    @Override
    public void edit(EditQuotaStorageModel object) {
        driver.edit(object);
    }

    @Override
    public EditQuotaStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}
