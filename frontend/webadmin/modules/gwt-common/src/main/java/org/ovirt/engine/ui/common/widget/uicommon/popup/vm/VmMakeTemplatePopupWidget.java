package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
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
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

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
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "dataCenterWithClustersList.selectedItem")
    @WithElementId("dataCenterWithCluster")
    public ListModelTypeAheadListBoxEditor<DataCenterWithCluster> clusterEditor;

    @UiField(provided = true)
    @Path(value = "isSubTemplate.entity")
    @WithElementId("isSubTemplate")
    EntityModelCheckBoxEditor isSubTemplateEditor;

    @UiField
    @Ignore
    Panel subTemplateExpanderContent;

    @UiField(provided = true)
    @Path(value = "baseTemplate.selectedItem")
    @WithElementId("baseTemplate")
    public ListModelTypeAheadListBoxEditor<VmTemplate> baseTemplateEditor;

    @UiField
    @Path(value = "templateVersionName.entity")
    @WithElementId("templateVersionName")
    StringEntityModelTextBoxEditor templateVersionNameEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    DisksAllocationView disksAllocationView;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Quota> quotaEditor;

    @UiField(provided = true)
    @Path(value = "isTemplatePublic.entity")
    @WithElementId("isTemplatePublic")
    EntityModelCheckBoxEditor isTemplatePublicEditor;

    @UiField(provided = true)
    @Path(value = "copyPermissions.entity")
    @WithElementId("copyVmPermissions")
    EntityModelCheckBoxEditor copyVmPermissions;

    @UiField(provided = true)
    @Path(value = "cpuProfiles.selectedItem")
    @WithElementId("cpuProfiles")
    public ListModelListBoxEditor<CpuProfile> cpuProfilesEditor;

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
        isSubTemplateEditor.setContentWidgetStyleName(style.editorLabel());
    }

    void initListBoxEditors() {
        clusterEditor = new ListModelTypeAheadListBoxEditor<DataCenterWithCluster>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<DataCenterWithCluster>() {

                    @Override
                    public String getReplacementStringNullSafe(DataCenterWithCluster data) {
                        return data.getCluster().getName() + "/" //$NON-NLS-1$
                                + data.getDataCenter().getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(DataCenterWithCluster data) {

                        String clusterName = data.getCluster().getName();
                        String dcName = data.getDataCenter().getName();
                        String dcDescription = data.getDataCenter().getdescription();
                        // description takes priority
                        String dcString = !StringHelper.isNullOrEmpty(dcDescription) ? dcDescription : dcName;

                        return applicationTemplates.typeAheadNameDescription(clusterName == null ? "" : clusterName,
                                dcString == null ? "" : dcName).asString();
                    }

                });

        quotaEditor = new ListModelListBoxEditor<Quota>(new NullSafeRenderer<Quota>() {
            @Override
            public String renderNullSafe(Quota object) {
                return object.getQuotaName();
            }
        });

        baseTemplateEditor = new ListModelTypeAheadListBoxEditor<VmTemplate>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<VmTemplate>() {

                    @Override
                    public String getReplacementStringNullSafe(VmTemplate data) {
                        return data.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(VmTemplate data) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                data.getName(),
                                data.getDescription()
                        );
                    }
                });

        cpuProfilesEditor = new ListModelListBoxEditor<CpuProfile>(new NullSafeRenderer<CpuProfile>() {
            @Override
            protected String renderNullSafe(CpuProfile object) {
                return object.getName();
            }
        });
    }

    private String typeAheadNameDescriptionTemplateNullSafe(String name, String description) {
        return applicationTemplates.typeAheadNameDescription(
                name != null ? name : "",
                description != null ? description : "")
                .asString();
    }

    void initCheckBoxEditors() {
        isTemplatePublicEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        copyVmPermissions = new EntityModelCheckBoxEditor(Align.RIGHT);
        isSubTemplateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
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
        isSubTemplateEditor.setLabel(constants.createAsSubTemplate());
        baseTemplateEditor.setLabel(constants.rootTemplate());
        templateVersionNameEditor.setLabel(constants.templateVersionName());
        cpuProfilesEditor.setLabel(constants.cpuProfileLabel());
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
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if ("Message".equals(propName)) { //$NON-NLS-1$
                    appendMessage(model.getMessage());
                }
            }
        });

        subTemplateExpanderContent.setVisible(false);
        model.getIsSubTemplate().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                subTemplateExpanderContent.setVisible(model.getIsSubTemplate().getEntity());
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
        cpuProfilesEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        isSubTemplateEditor.setTabIndex(nextTabIndex++);
        baseTemplateEditor.setTabIndex(nextTabIndex++);
        templateVersionNameEditor.setTabIndex(nextTabIndex++);
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
