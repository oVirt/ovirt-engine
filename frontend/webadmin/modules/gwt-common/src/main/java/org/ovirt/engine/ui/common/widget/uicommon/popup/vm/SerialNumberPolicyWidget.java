package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SerialNumberPolicyModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;

public class SerialNumberPolicyWidget extends AbstractModelBoundPopupWidget<SerialNumberPolicyModel>
        implements HasEnabled {

    interface Driver extends SimpleBeanEditorDriver<SerialNumberPolicyModel, SerialNumberPolicyWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, SerialNumberPolicyWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SerialNumberPolicyWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Path("overrideSerialNumberPolicy.entity")
    @WithElementId
    public EntityModelCheckBoxOnlyEditor overrideSerialNumberPolicy;

    @UiField(provided = true)
    @Path("serialNumberPolicy.entity")
    public EnumRadioEditor<SerialNumberPolicy> serialNumberPolicy;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo overrideSerialNumberPolicyWithInfo;

    @UiField
    @Path("customSerialNumber.entity")
    public StringEntityModelTextBoxOnlyEditor customSerialNumber;

    private final EnableableFormLabel label;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public SerialNumberPolicyWidget() {
        overrideSerialNumberPolicy = new EntityModelCheckBoxOnlyEditor() {
            @Override
            public void setUsePatternFly(final boolean use) {
                if (use) {
                    noPaddingNoFixes();
                }
            }
        };
        label = new EnableableFormLabel();
        label.setText(constants.overrideSerialNumberPolicy());
        overrideSerialNumberPolicyWithInfo = new EntityModelWidgetWithInfo(label, overrideSerialNumberPolicy, Align.LEFT);
        overrideSerialNumberPolicyWithInfo.setExplanation(templates.italicText(messages.serialNumberInfo()));

        serialNumberPolicy = new EnumRadioEditor<>(SerialNumberPolicy.class);


        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    public void setUsePatternFly(boolean use) {
        overrideSerialNumberPolicyWithInfo.setUsePatternFly(use);
        overrideSerialNumberPolicy.setUsePatternFly(use);
        if (use) {
            label.getElement().getStyle().setPaddingLeft(10, Unit.PX);
            label.getElement().getStyle().setPaddingRight(10, Unit.PX);
            label.getElement().getStyle().setPosition(Position.RELATIVE);
        }
    }

    @Override
    public void edit(SerialNumberPolicyModel model) {
        driver.edit(model);
    }

    @Override
    public SerialNumberPolicyModel flush() {
        return driver.flush();
    }

    public void setRenderer(VisibilityRenderer renderer) {
        overrideSerialNumberPolicy.setRenderer(renderer);
    }

    @Override
    public boolean isEnabled() {
        return serialNumberPolicy.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        overrideSerialNumberPolicy.setEnabled(enabled);
        serialNumberPolicy.setEnabled(enabled);
        customSerialNumber.setEnabled(enabled);
    }
}
