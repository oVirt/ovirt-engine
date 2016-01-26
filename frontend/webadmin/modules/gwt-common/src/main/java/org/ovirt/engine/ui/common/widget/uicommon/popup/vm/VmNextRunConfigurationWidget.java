package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class VmNextRunConfigurationWidget extends AbstractModelBoundPopupWidget<VmNextRunConfigurationModel> {

    interface Driver extends SimpleBeanEditorDriver<VmNextRunConfigurationModel, VmNextRunConfigurationWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmNextRunConfigurationWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmNextRunConfigurationWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel changedFieldsPanel;

    @UiField
    @Ignore
    HTML changedFieldsPanelTitle;

    @UiField
    @Ignore
    HTML applyNowTitleMessage;

    @UiField
    @Ignore
    HTML applyNowCpuMessage;

    @UiField
    @Ignore
    HTML applyNowMemoryMessage;

    @UiField
    @Ignore
    HTML changedFields;

    @UiField(provided = true)
    @Path(value = "applyCpuLater.entity")
    EntityModelCheckBoxEditor applyCpuLaterEditor;

    @UiField
    FlowPanel hotplugPanel;

    @UiField
    @Ignore
    FlowPanel changedFieldsContent;

    @UiField
    FlowPanel vmUnpinnedPanel;

    @UiField
    @Ignore
    HTML vmUnpinnedPanelTitle;

    @UiField
    @Ignore
    HTML vmUnpinnedMessage1;

    @UiField
    @Ignore
    HTML vmUnpinnedMessage2;

    @UiField
    @Ignore
    HTML warningSectionTitle;

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Path(value = "latch.entity")
    EntityModelCheckBoxEditor vmUnpinnedLatchEditor;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public VmNextRunConfigurationWidget() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        setVisibilityToChangedFieldsExpander(false);
        setVisibilityToVmUnpinningWarrningPanel(false);
    }

    void initEditors() {
        applyCpuLaterEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vmUnpinnedLatchEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize() {
        changedFieldsPanelTitle.setHTML(messages.nextRunConfigurationExists());
        applyNowTitleMessage.setHTML(messages.nextRunConfigurationCanBeAppliedImmediately());
        applyNowCpuMessage.setHTML(bulletedItem(messages.nextRunConfigurationCpuValue()));
        applyNowMemoryMessage.setHTML(bulletedItem(messages.nextRunConfigurationMemoryValue()));
        applyCpuLaterEditor.setLabel(constants.applyLater());

        warningSectionTitle.setHTML(messages.warningSectionTitle());
        vmUnpinnedPanelTitle.setHTML(messages.unpinnedRunningVmWarningTitle());
        vmUnpinnedMessage1.setHTML(bulletedItem(messages.unpinnedRunningVmWarningIncompatability()));
        vmUnpinnedMessage2.setHTML(bulletedItem(messages.unpinnedRunningVmWarningSecurity()));
        vmUnpinnedLatchEditor.setLabel(constants.latchApproveUnpinningLabel());
}

    private SafeHtml bulletedItem(String msg) {
        return templates.unorderedList(templates.listItem(SafeHtmlUtils.fromSafeConstant(msg)));
    }

    @Override
    public void edit(VmNextRunConfigurationModel object) {
        driver.edit(object);

        if (object.isVmUnpinned()){
            setVisibilityToVmUnpinningWarrningPanel(true);
        }

        if (object.getChangedFields().size() > 0) {
            setVisibilityToChangedFieldsExpander(true);
            SafeHtmlBuilder changedFieldsBuilder = new SafeHtmlBuilder();
            for (String field: object.getChangedFields()) {
                String escapedField = SafeHtmlUtils.htmlEscape(field);
                changedFieldsBuilder.append(bulletedItem(escapedField));
            }
            changedFields.setHTML(changedFieldsBuilder.toSafeHtml());
        }
        setVisibilityToHotChanges(object);
    }

    private void setVisibilityToHotChanges(VmNextRunConfigurationModel object) {
        hotplugPanel.setVisible(object.isCpuPluggable() || object.isMemoryPluggable());
        applyNowCpuMessage.setVisible(object.isCpuPluggable());
        applyNowMemoryMessage.setVisible(object.isMemoryPluggable());
    }

    @Override
    public VmNextRunConfigurationModel flush() {
        return driver.flush();
    }

    private void setVisibilityToVmUnpinningWarrningPanel(boolean visibility) {
        vmUnpinnedPanel.setVisible(visibility);
        vmUnpinnedPanelTitle.setVisible(visibility);
        vmUnpinnedLatchEditor.setVisible(visibility);
    }

    private void setVisibilityToChangedFieldsExpander(boolean flag) {
        changedFieldsPanel.setVisible(flag);
        applyNowTitleMessage.setVisible(flag);
        changedFieldsPanelTitle.setVisible(flag);
    }
}
