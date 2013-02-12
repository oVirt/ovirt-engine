package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmSnapshotCreatePopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends SimpleBeanEditorDriver<SnapshotModel, VmSnapshotCreatePopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, VmSnapshotCreatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotCreatePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    public VmSnapshotCreatePopupWidget(CommonApplicationConstants constants) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        Driver.driver.initialize(this);
    }

    void localize(CommonApplicationConstants constants) {
        descriptionEditor.setLabel(constants.virtualMachineSnapshotCreatePopupDescriptionLabel());
    }

    @Override
    public void edit(final SnapshotModel model) {
        Driver.driver.edit(model);

        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("Message".equals(propName)) { //$NON-NLS-1$
                    appendMessage(model.getMessage());
                }
            }
        });
    }

    @Override
    public SnapshotModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}
