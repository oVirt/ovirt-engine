package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.quota.EditQuotaStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.EditQuotaStoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class EditQuotaStoragePopupView extends AbstractModelBoundPopupView<EditQuotaStorageModel> implements EditQuotaStoragePopupPresenterWidget.ViewDef {

    @UiField
    WidgetStyle style;

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
    EntityModelTextBoxEditor storageValueEditor;

    @UiField
    @Ignore
    Label storageLabel;

    interface Driver extends SimpleBeanEditorDriver<EditQuotaStorageModel, EditQuotaStoragePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, EditQuotaStoragePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<EditQuotaStoragePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public EditQuotaStoragePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    private void addStyles() {
        storageValueEditor.addContentWidgetStyleName(style.textBoxWidth());
        storageValueEditor.addLabelStyleName(style.labelVisible());
        specificStorageRadioButtonEditor.addContentWidgetStyleName(style.radioButtonWidth());
        unlimitedStorageRadioButtonEditor.addContentWidgetStyleName(style.radioButtonWidth());
    }

    private void initRadioButtonEditors() {
        unlimitedStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
        specificStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
    }

    void localize(ApplicationConstants constants) {
        unlimitedStorageRadioButtonEditor.setLabel(constants.ultQuotaPopup());
        specificStorageRadioButtonEditor.setLabel(constants.useQuotaPopup());
        storageLabel.setText(constants.storageQuotaQuotaPopup());
    }

    @Override
    public void edit(EditQuotaStorageModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public EditQuotaStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String textBoxWidth();

        String radioButtonWidth();

        String labelVisible();
    }
}
