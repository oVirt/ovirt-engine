package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SerialNumberPolicyModel;

public class SerialNumberPolicyWidget extends AbstractModelBoundPopupWidget<SerialNumberPolicyModel> {

    interface Driver extends SimpleBeanEditorDriver<SerialNumberPolicyModel, SerialNumberPolicyWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, SerialNumberPolicyWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SerialNumberPolicyWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String serialNumberPolicy();
    }

    @UiField
    public Style style;

    @UiField(provided = true)
    @Path("overrideSerialNumberPolicy.entity")
    public EntityModelCheckBoxEditor overrideSerialNumberPolicy;

    @UiField(provided = true)
    public InfoIcon serialNumberInfoIcon;

    @UiField(provided = true)
    @Path("serialNumberPolicy.entity")
    public EnumRadioEditor<SerialNumberPolicy> serialNumberPolicy;

    @UiField
    @Path("customSerialNumber.entity")
    public StringEntityModelTextBoxOnlyEditor customSerialNumber;

    public SerialNumberPolicyWidget(EventBus eventBus,
                                    CommonApplicationTemplates applicationTemplates,
                                    CommonApplicationMessages applicationMessages,
                                    CommonApplicationResources applicationResources,
                                    VisibilityRenderer visibilityRenderer) {
        overrideSerialNumberPolicy = new EntityModelCheckBoxEditor(Align.RIGHT, visibilityRenderer, false);
        serialNumberPolicy = new EnumRadioEditor<SerialNumberPolicy>(SerialNumberPolicy.class);
        serialNumberInfoIcon = new InfoIcon(applicationTemplates.italicText(applicationMessages.serialNumberInfo()), applicationResources);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);

        serialNumberPolicy.asWidget().addStyleName(style.serialNumberPolicy());
    }

    @Override
    public void edit(SerialNumberPolicyModel model) {
        driver.edit(model);
    }

    @Override
    public SerialNumberPolicyModel flush() {
        return driver.flush();
    }

    public void setCheckboxStyle(String checkboxStyle) {
        overrideSerialNumberPolicy.addStyleName(checkboxStyle);
    }
}
