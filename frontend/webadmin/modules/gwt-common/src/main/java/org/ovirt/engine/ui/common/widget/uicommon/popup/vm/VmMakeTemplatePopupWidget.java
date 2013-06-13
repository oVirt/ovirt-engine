package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmMakeTemplatePopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, VmMakeTemplatePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmMakeTemplatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmMakeTemplatePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    EntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "dataCenterWithClustersList.selectedItem")
    @WithElementId("dataCenterWithCluster")
    public ListModelTypeAheadListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    DisksAllocationView disksAllocationView;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    @Path(value = "isTemplatePublic.entity")
    @WithElementId("isTemplatePublic")
    EntityModelCheckBoxEditor isTemplatePublicEditor;

    @UiField(provided = true)
    @Path(value = "copyPermissions.entity")
    @WithElementId("copyVmPermissions")
    EntityModelCheckBoxEditor copyVmPermissions;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    @UiField
    @Ignore
    Label disksAllocationLabel;

    interface WidgetStyle extends CssResource {
        String editorLabel();
    }

    @UiField
    WidgetStyle style;

    private final Driver driver = GWT.create(Driver.class);

    private final CommonApplicationTemplates applicationTemplates;

    public VmMakeTemplatePopupWidget(CommonApplicationConstants constants,
            CommonApplicationTemplates applicationTemplates) {
        this.applicationTemplates = applicationTemplates;
        initListBoxEditors();
        initCheckBoxEditors();
        disksAllocationView = new DisksAllocationView(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        addStyle();
    }

    void addStyle() {
        isTemplatePublicEditor.setContentWidgetStyleName(style.editorLabel());
        copyVmPermissions.setContentWidgetStyleName(style.editorLabel());
    }

    void initListBoxEditors() {
        clusterEditor = new ListModelTypeAheadListBoxEditor<Object>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<Object>() {

                    @Override
                    public String getReplacementStringNullSafe(Object data) {
                        return ((DataCenterWithCluster) data).getCluster().getName() + "/" //$NON-NLS-1$
                                + ((DataCenterWithCluster) data).getDataCenter().getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(Object data) {

                        String clusterName = ((DataCenterWithCluster) data).getCluster().getName();
                        String dcName = ((DataCenterWithCluster) data).getDataCenter().getName();
                        String dcDescription =
                                ((DataCenterWithCluster) data).getDataCenter().getdescription();
                        // description takes priority
                        String dcString = !StringHelper.isNullOrEmpty(dcDescription) ? dcDescription : dcName;

                        return applicationTemplates.typeAheadNameDescription(clusterName == null ? "" : clusterName,
                                dcString == null ? "" : dcName).asString();
                    }

                });

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
    }

    void initCheckBoxEditors() {
        isTemplatePublicEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        copyVmPermissions = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(CommonApplicationConstants constants) {
        nameEditor.setLabel(constants.makeTemplatePopupNameLabel());
        descriptionEditor.setLabel(constants.makeTemplatePopupDescriptionLabel());
        commentEditor.setLabel(constants.commentLabel());
        clusterEditor.setLabel(constants.makeTemplateClusterLabel());
        quotaEditor.setLabel(constants.makeTemplateQuotaLabel());
        isTemplatePublicEditor.setLabel(constants.makeTemplateIsTemplatePublicEditorLabel());
        copyVmPermissions.setLabel(constants.copyVmPermissions());
        disksAllocationLabel.setText(constants.disksAllocation());
    }

    @Override
    public void edit(final UnitVmModel model) {
        driver.edit(model);

        model.getStorageDomain().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                addDiskAllocation(model);
            }
        });

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

    private void addDiskAllocation(UnitVmModel model) {
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    @Override
    public UnitVmModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        commentEditor.setTabIndex(nextTabIndex++);
        clusterEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = disksAllocationView.setTabIndexes(nextTabIndex);
        isTemplatePublicEditor.setTabIndex(nextTabIndex++);
        copyVmPermissions.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}
