package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class VmNextRunConfigurationWidget extends AbstractModelBoundPopupWidget<VmNextRunConfigurationModel> {

    private final CommonApplicationTemplates templates;

    interface Driver extends SimpleBeanEditorDriver<VmNextRunConfigurationModel, VmNextRunConfigurationWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmNextRunConfigurationWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmNextRunConfigurationWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    HTML message1;

    @UiField
    @Ignore
    HTML message2;

    @UiField
    @Ignore
    HTML changedFields;

    @UiField(provided = true)
    @Path(value = "applyCpuLater.entity")
    EntityModelCheckBoxEditor applyCpuLaterEditor;

    @UiField
    FlowPanel cpuPanel;

    @UiField
    @Ignore
    AdvancedParametersExpander changedFieldsExpander;

    @UiField
    @Ignore
    FlowPanel changedFieldsExpanderContent;

    private final Driver driver = GWT.create(Driver.class);

    public VmNextRunConfigurationWidget(CommonApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants, messages, templates);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        changedFieldsExpander.initWithContent(changedFieldsExpanderContent.getElement());
        this.templates = templates;
    }

    void initEditors() {
        applyCpuLaterEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(CommonApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        message1.setHTML(listItem(messages.nextRunConfigurationExists(), templates));
        message2.setHTML(listItem(messages.nextRunConfigurationCanBeAppliedImmediately(), templates));
        applyCpuLaterEditor.setLabel(constants.applyLater());

        changedFieldsExpander.setTitleWhenExpended(constants.changedFieldsList());
        changedFieldsExpander.setTitleWhenCollapsed(constants.changedFieldsList());
    }

    private SafeHtml listItem(String msg, CommonApplicationTemplates templates) {
        return templates.listItem(SafeHtmlUtils.fromSafeConstant(msg));
    }

    @Override
    public void edit(VmNextRunConfigurationModel object) {
        driver.edit(object);
        cpuPanel.setVisible(object.isCpuPluggable());

        SafeHtmlBuilder changedFieldsBuilder = new SafeHtmlBuilder();
        for (String field: object.getChangedFields()) {
            String escapedField = SafeHtmlUtils.htmlEscape(field);
            changedFieldsBuilder.append(listItem(escapedField, templates));
        }
        changedFields.setHTML(changedFieldsBuilder.toSafeHtml());
    }

    @Override
    public VmNextRunConfigurationModel flush() {
        return driver.flush();
    }

}
