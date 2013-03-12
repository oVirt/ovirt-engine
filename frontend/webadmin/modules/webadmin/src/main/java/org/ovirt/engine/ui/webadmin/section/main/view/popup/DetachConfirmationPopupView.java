package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DetachConfirmationPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class DetachConfirmationPopupView extends AbstractModelBoundPopupView<HostInterfaceModel> implements DetachConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostInterfaceModel, DetachConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DetachConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    HorizontalPanel itemPanel;

    @UiField
    @Path(value = "name.entity")
    EntityModelLabelEditor item;

    @UiField
    @Ignore
    HTML info;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public DetachConfirmationPopupView(EventBus eventBus, ApplicationResources resources, ApplicationMessages messages, ApplicationConstants constants) {
        super(eventBus, resources);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        message.setText(constants.areYouSureDetachConfirmPopup());

        info.setHTML(constants.changesTempWarningDetachConfirmPopup());
        commitChanges.setLabel(constants.saveNetCongDetachConfirmPopup());

        driver.initialize(this);
    }

    @Override
    public void edit(HostInterfaceModel object) {
        driver.edit(object);
    }

    @Override
    public HostInterfaceModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        commitChanges.setFocus(true);
    }

}
