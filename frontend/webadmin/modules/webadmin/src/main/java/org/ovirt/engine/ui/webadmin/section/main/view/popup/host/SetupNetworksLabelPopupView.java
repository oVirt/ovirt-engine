package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksLabelModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class SetupNetworksLabelPopupView extends AbstractModelBoundPopupView<SetupNetworksLabelModel> implements SetupNetworksLabelPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<SetupNetworksLabelModel, SetupNetworksLabelPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, SetupNetworksLabelPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "label.entity")
    StringEntityModelTextBoxEditor label;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SetupNetworksLabelPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(SetupNetworksLabelModel object) {
        driver.edit(object);
    }

    @Override
    public SetupNetworksLabelModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        super.focusInput();
        label.setFocus(true);
    }

}
