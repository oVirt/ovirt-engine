package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class CloneVmWidget extends AbstractModelBoundPopupWidget<CloneVmModel> {

    interface Driver extends SimpleBeanEditorDriver<CloneVmModel, CloneVmWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, CloneVmWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CloneVmWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "cloneName.entity")
    @WithElementId("cloneName")
    StringEntityModelTextBoxEditor cloneNameEditor;

    @UiField
    @WithElementId("Message")
    FlowPanel messagePanel;

    public CloneVmWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

    @Override
    public void edit(final CloneVmModel object) {
        driver.edit(object);

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if ("Message".equals(propName)) { //$NON-NLS-1$
                    appendMessage(object.getMessage());
                }
            }
        });
    }

    @Override
    public CloneVmModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        cloneNameEditor.setFocus(true);
    }
}
