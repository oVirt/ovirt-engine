package org.ovirt.engine.ui.common.view.popup;

import org.gwtbootstrap3.client.ui.FormLabel;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.section.main.presenter.OptionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextArea;
import org.ovirt.engine.ui.uicommonweb.models.EditOptionsModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class OptionsPopupView extends AbstractModelBoundPopupView<EditOptionsModel> implements OptionsPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<OptionsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends SimpleBeanEditorDriver<EditOptionsModel, OptionsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, OptionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "enableConnectAutomatically.entity")
    @WithElementId("enableConnectAutomatically")
    EntityModelCheckBoxOnlyEditor connectAutomaticallyEditor;

    @UiField
    @Ignore
    public FormLabel connectAutomaticallyLabel;

    @UiField
    @Path(value = "publicKey.entity")
    StringEntityModelTextArea publicKeyEditor;

    @Inject
    public OptionsPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        final String checkboxId = connectAutomaticallyEditor.asCheckBox().getElement().getFirstChildElement().getId();
        connectAutomaticallyLabel.setFor(checkboxId);
        driver.initialize(this);
    }

    @Override
    public HasChangeHandlers getPublicKeyEditor() {
        return publicKeyEditor;
    }

    @Override
    public void edit(final EditOptionsModel model) {
        driver.edit(model);
    }

    @Override
    public EditOptionsModel flush() {
        return driver.flush();
    }
}
