package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.MissingResourceException;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.NextRunFieldMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class VmNextRunConfigurationWidget extends AbstractModelBoundPopupWidget<VmNextRunConfigurationModel> {

    interface Driver extends UiCommonEditorDriver<VmNextRunConfigurationModel, VmNextRunConfigurationWidget> {
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
    Label changedFieldsPanelTitle;

    @UiField
    @Ignore
    HTML applyNowCpuMessage;

    @UiField
    @Ignore
    HTML applyNowMemoryMessage;

    @UiField
    @Ignore
    HTML applyNowMinAllocatedMemoryMessage;

    @UiField
    @Ignore
    HTML applyNowVmLeaseMessage;

    @UiField
    @Ignore
    HTML changedFields;

    @UiField(provided = true)
    @Path(value = "applyLater.entity")
    EntityModelCheckBoxEditor applyLaterEditor;

    @UiField
    FlowPanel hotplugPanel;

    @UiField
    @Ignore
    FlowPanel changedFieldsContent;

    @UiField
    FlowPanel vmUnpinnedPanel;

    @UiField
    @Ignore
    Label vmUnpinnedPanelTitle;

    @UiField
    @Ignore
    HTML vmUnpinnedMessage1;

    @UiField
    @Ignore
    HTML vmUnpinnedMessage2;

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Path(value = "latch.entity")
    EntityModelCheckBoxEditor vmUnpinnedLatchEditor;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final NextRunFieldMessages nextRunMessages = ConstantsManager.getInstance().getNextRunFieldMessages();

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
        applyLaterEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vmUnpinnedLatchEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize() {
        applyNowCpuMessage.setHTML(bulletedItem(messages.nextRunConfigurationCpuValue()));
        applyNowMemoryMessage.setHTML(bulletedItem(messages.nextRunConfigurationMemoryValue()));
        applyNowMinAllocatedMemoryMessage.setHTML(bulletedItem(messages.nextRunConfigurationMinAllocatedMemoryValue()));
        applyNowVmLeaseMessage.setHTML(bulletedItem(messages.nextRunConfigurationVmLeaseValue()));
        vmUnpinnedMessage1.setHTML(bulletedItem(messages.unpinnedRunningVmWarningIncompatability()));
        vmUnpinnedMessage2.setHTML(bulletedItem(messages.unpinnedRunningVmWarningSecurity()));
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
                String msg = getNextRunMessage(field);
                String escapedField = SafeHtmlUtils.htmlEscape(msg);
                changedFieldsBuilder.append(bulletedItem(escapedField));
            }
            changedFields.setHTML(changedFieldsBuilder.toSafeHtml());
        }
        setVisibilityToHotChanges(object);
    }

    private String getNextRunMessage(String field) {
        try {
            return nextRunMessages.getString(field);
        } catch (MissingResourceException e) {
            // ignore
        }
        return field;
    }

    private void setVisibilityToHotChanges(VmNextRunConfigurationModel object) {
        hotplugPanel.setVisible(object.isAnythingPluggable());
        applyNowCpuMessage.setVisible(object.isCpuPluggable());
        applyNowMemoryMessage.setVisible(object.isMemoryPluggable());
        applyNowMinAllocatedMemoryMessage.setVisible(object.isMinAllocatedMemoryPluggable());
        applyNowVmLeaseMessage.setVisible(object.isVmLeaseUpdated());
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
        changedFieldsPanelTitle.setVisible(flag);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
