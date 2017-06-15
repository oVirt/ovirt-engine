package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class VmMakeTemplatePopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends UiCommonEditorDriver<UnitVmModel, VmMakeTemplatePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<Container, VmMakeTemplatePopupWidget> {
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
    @Path(value = "sealTemplate.entity")
    @WithElementId("sealTemplate")
    EntityModelCheckBoxEditor sealTemplateEditor;

    @UiField(provided = true)
    @Path(value = "cpuProfiles.selectedItem")
    @WithElementId("cpuProfiles")
    public ListModelListBoxEditor<CpuProfile> cpuProfilesEditor;

    @UiField
    @Ignore
    Alert messagePanel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    public VmMakeTemplatePopupWidget() {
        initListBoxEditors();
        initCheckBoxEditors();
        disksAllocationView = new DisksAllocationView();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void initListBoxEditors() {
        clusterEditor = new ListModelTypeAheadListBoxEditor<>(
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

                        return templates.typeAheadNameDescription(clusterName == null ? "" : clusterName,
                                dcString == null ? "" : dcName).asString();
                    }

                });

        quotaEditor = new ListModelListBoxEditor<>(new NameRenderer<Quota>());

        baseTemplateEditor = new ListModelTypeAheadListBoxEditor<>(
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

        cpuProfilesEditor = new ListModelListBoxEditor<>(new NameRenderer<CpuProfile>());
    }

    private String typeAheadNameDescriptionTemplateNullSafe(String name, String description) {
        return templates.typeAheadNameDescription(
                name != null ? name : "",
                description != null ? description : "")
                .asString();
    }

    void initCheckBoxEditors() {
        isTemplatePublicEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        copyVmPermissions = new EntityModelCheckBoxEditor(Align.RIGHT);
        sealTemplateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isSubTemplateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    @Override
    public void edit(final UnitVmModel model) {
        driver.edit(model);

        model.getStorageDomain().getItemsChangedEvent().addListener((ev, sender, args) -> addDiskAllocation(model));

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("Message".equals(propName)) { //$NON-NLS-1$
                appendMessage(model.getMessage());
            }
        });

        subTemplateExpanderContent.setVisible(false);
        model.getIsSubTemplate().getEntityChangedEvent().addListener((ev, sender, args) -> subTemplateExpanderContent.setVisible(model.getIsSubTemplate().getEntity()));
    }

    private void addDiskAllocation(UnitVmModel model) {
        if (model.getDisks().size() > 0) {
            disksAllocationView.edit(model.getDisksAllocationModel());
            model.getDisksAllocationModel().setDisks(model.getDisks());
        }
    }

    @Override
    public UnitVmModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
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
        sealTemplateEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}
