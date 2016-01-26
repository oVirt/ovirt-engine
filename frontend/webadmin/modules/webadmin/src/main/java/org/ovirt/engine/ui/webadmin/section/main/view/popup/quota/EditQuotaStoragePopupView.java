package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.LongEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.quota.EditQuotaStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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
    LongEntityModelTextBoxEditor storageValueEditor;

    @UiField
    @Ignore
    Label storageLabel;

    interface Driver extends SimpleBeanEditorDriver<EditQuotaStorageModel, EditQuotaStoragePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, EditQuotaStoragePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<EditQuotaStoragePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public EditQuotaStoragePopupView(EventBus eventBus) {
        super(eventBus);
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        addStyles();
        driver.initialize(this);
    }

    private void addStyles() {
        storageValueEditor.addContentWidgetContainerStyleName(style.textBoxWidth());
        storageValueEditor.addLabelStyleName(style.labelVisible());
        specificStorageRadioButtonEditor.addContentWidgetContainerStyleName(style.radioButtonWidth());
        unlimitedStorageRadioButtonEditor.addContentWidgetContainerStyleName(style.radioButtonWidth());
    }

    private void initRadioButtonEditors() {
        unlimitedStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
        specificStorageRadioButtonEditor = new EntityModelRadioButtonEditor("5"); //$NON-NLS-1$
    }

    void localize() {
        unlimitedStorageRadioButtonEditor.setLabel(constants.ultQuotaPopup());
        specificStorageRadioButtonEditor.setLabel(constants.useQuotaPopup());
        storageLabel.setText(constants.storageQuotaQuotaPopup());
    }

    @Override
    public void edit(EditQuotaStorageModel object) {
        driver.edit(object);
    }

    @Override
    public EditQuotaStorageModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String textBoxWidth();

        String radioButtonWidth();

        String labelVisible();
    }

}
