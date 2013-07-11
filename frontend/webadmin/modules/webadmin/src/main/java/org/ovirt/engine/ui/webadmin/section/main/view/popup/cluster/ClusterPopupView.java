package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class ClusterPopupView extends AbstractModelBoundPopupView<ClusterModel> implements ClusterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterModel, ClusterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ClusterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    DialogTabPanel tabsPanel;

    @UiField
    WidgetStyle style;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    FlowPanel dataCenterPanel;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId
    EntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "cPU.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> cPUEditor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> versionEditor;

    @UiField
    @Ignore
    VerticalPanel servicesCheckboxPanel;

    @UiField
    @Path(value = "enableOvirtService.entity")
    @WithElementId("enableOvirtService")
    EntityModelCheckBoxEditor enableOvirtServiceEditor;

    @UiField
    @Path(value = "enableGlusterService.entity")
    @WithElementId("enableGlusterService")
    EntityModelCheckBoxEditor enableGlusterServiceEditor;

    @UiField
    @Ignore
    VerticalPanel servicesRadioPanel;

    @UiField(provided = true)
    @Path(value = "enableOvirtService.entity")
    @WithElementId("enableOvirtServiceOption")
    EntityModelRadioButtonEditor enableOvirtServiceOptionEditor;

    @UiField(provided = true)
    @Path(value = "enableGlusterService.entity")
    @WithElementId("enableGlusterServiceOption")
    EntityModelRadioButtonEditor enableGlusterServiceOptionEditor;

    @UiField(provided = true)
    @Path(value = "isImportGlusterConfiguration.entity")
    @WithElementId("isImportGlusterConfiguration")
    EntityModelCheckBoxEditor importGlusterConfigurationEditor;

    @UiField
    @Ignore
    Label importGlusterExplanationLabel;

    @UiField
    @Path(value = "glusterHostAddress.entity")
    @WithElementId
    EntityModelTextBoxEditor glusterHostAddressEditor;

    @UiField
    @Path(value = "glusterHostFingerprint.entity")
    @WithElementId
    EntityModelTextAreaLabelEditor glusterHostFingerprintEditor;

    @UiField
    @Path(value = "glusterHostPassword.entity")
    @WithElementId
    EntityModelPasswordBoxEditor glusterHostPasswordEditor;

    @UiField
    @Ignore
    Label messageLabel;

    @UiField
    @WithElementId
    DialogTab optimizationTab;

    @UiField
    @Ignore
    Label memoryOptimizationPanelTitle;

    @UiField(provided = true)
    InfoIcon memoryOptimizationInfo;

    @UiField(provided = true)
    @Path(value = "optimizationNone_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationNoneEditor;

    @UiField(provided = true)
    @Path(value = "optimizationForServer_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForServerEditor;

    @UiField(provided = true)
    @Path(value = "optimizationForDesktop_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForDesktopEditor;

    @UiField(provided = true)
    @Path(value = "optimizationCustom_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationCustomEditor;

    @UiField
    FlowPanel cpuThreadsPanel;

    @UiField
    @Ignore
    Label cpuThreadsPanelTitle;

    @UiField(provided = true)
    InfoIcon cpuThreadsInfo;

    @UiField(provided = true)
    @Path(value = "countThreadsAsCores.entity")
    @WithElementId
    EntityModelCheckBoxEditor countThreadsAsCoresEditor;

    @UiField
    @WithElementId
    DialogTab resiliencePolicyTab;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_YES.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_YESEditor;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_HA_ONLY.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_HA_ONLYEditor;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_NO.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_NOEditor;

    @UiField
    @WithElementId
    DialogTab clusterPolicyTab;

    @UiField
    @Ignore
    Label additionPropsPanelTitle;

    @UiField(provided = true)
    @Path(value = "enableTrustedService.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableTrustedServiceEditor;

    @UiField(provided = true)
    @Path(value = "clusterPolicy.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> clusterPolicyEditor;

    @UiField
    @Ignore
    protected KeyValueWidget customPropertiesSheetEditor;

    @UiField(provided = true)
    @Path(value = "enableBallooning.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableBallooning;

    private final Driver driver = GWT.create(Driver.class);

    private final ApplicationMessages messages;

    @Inject
    public ClusterPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationMessages messages, ApplicationTemplates templates) {
        super(eventBus, resources);
        this.messages = messages;
        initListBoxEditors();
        initRadioButtonEditors();
        initCheckBoxEditors();
        initInfoIcons(resources, constants, templates);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        addStyles();
        localize(constants);
        driver.initialize(this);
        applyModeCustomizations();
    }

    private void addStyles() {
        importGlusterConfigurationEditor.addContentWidgetStyleName(style.editorContentWidget());
        migrateOnErrorOption_NOEditor.addContentWidgetStyleName(style.label());
        migrateOnErrorOption_YESEditor.addContentWidgetStyleName(style.label());
        migrateOnErrorOption_HA_ONLYEditor.addContentWidgetStyleName(style.label());

        optimizationNoneEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationForServerEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationForDesktopEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationCustomEditor.setContentWidgetStyleName(style.fullWidth());

        countThreadsAsCoresEditor.setContentWidgetStyleName(style.fullWidth());
        enableTrustedServiceEditor.setContentWidgetStyleName(style.fullWidth());

    }

    private void localize(ApplicationConstants constants) {
        generalTab.setLabel(constants.clusterPopupGeneralTabLabel());

        dataCenterEditor.setLabel(constants.clusterPopupDataCenterLabel());
        nameEditor.setLabel(constants.clusterPopupNameLabel());
        descriptionEditor.setLabel(constants.clusterPopupDescriptionLabel());
        commentEditor.setLabel(constants.commentLabel());
        cPUEditor.setLabel(constants.clusterPopupCPULabel());
        versionEditor.setLabel(constants.clusterPopupVersionLabel());
        enableOvirtServiceEditor.setLabel(constants.clusterEnableOvirtServiceLabel());
        enableGlusterServiceEditor.setLabel(constants.clusterEnableGlusterServiceLabel());
        enableOvirtServiceOptionEditor.setLabel(constants.clusterEnableOvirtServiceLabel());
        enableGlusterServiceOptionEditor.setLabel(constants.clusterEnableGlusterServiceLabel());

        importGlusterConfigurationEditor.setLabel(constants.clusterImportGlusterConfigurationLabel());
        importGlusterExplanationLabel.setText(constants.clusterImportGlusterConfigurationExplanationLabel());
        glusterHostAddressEditor.setLabel(constants.hostPopupHostAddressLabel());
        glusterHostFingerprintEditor.setLabel(constants.hostPopupHostFingerprintLabel());
        glusterHostPasswordEditor.setLabel(constants.hostPopupPasswordLabel());

        optimizationTab.setLabel(constants.clusterPopupOptimizationTabLabel());

        memoryOptimizationPanelTitle.setText(constants.clusterPopupMemoryOptimizationPanelTitle());
        optimizationNoneEditor.setLabel(constants.clusterPopupOptimizationNoneLabel());

        cpuThreadsPanelTitle.setText(constants.clusterPopupCpuThreadsPanelTitle());
        countThreadsAsCoresEditor.setLabel(constants.clusterPopupCountThreadsAsCoresLabel());

        resiliencePolicyTab.setLabel(constants.clusterPopupResiliencePolicyTabLabel());

        migrateOnErrorOption_YESEditor.setLabel(constants.clusterPopupMigrateOnError_YesLabel());
        migrateOnErrorOption_HA_ONLYEditor.setLabel(constants.clusterPopupMigrateOnError_HaLabel());
        migrateOnErrorOption_NOEditor.setLabel(constants.clusterPopupMigrateOnError_NoLabel());

        clusterPolicyTab.setLabel(constants.clusterPopupClusterPolicyTabLabel());

        additionPropsPanelTitle.setText(constants.clusterPolicyAdditionalPropsPanelTitle());
        enableTrustedServiceEditor.setLabel(constants.clusterPolicyEnableTrustedServiceLabel());
        clusterPolicyEditor.setLabel(constants.clusterPolicySelectPolicyLabel());

        enableBallooning.setLabel(constants.enableBallooningLabel());
    }

    private void initRadioButtonEditors() {
        enableOvirtServiceOptionEditor = new EntityModelRadioButtonEditor("service"); //$NON-NLS-1$
        enableGlusterServiceOptionEditor = new EntityModelRadioButtonEditor("service"); //$NON-NLS-1$

        optimizationNoneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        migrateOnErrorOption_YESEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        migrateOnErrorOption_HA_ONLYEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        migrateOnErrorOption_NOEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getName();
            }
        });

        cPUEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((ServerCpu) object).getCpuName();
            }
        });

        versionEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Version) object).toString();
            }
        });

        clusterPolicyEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((ClusterPolicy) object).getName();
            }
        });

    }

    private void initCheckBoxEditors()
    {
        importGlusterConfigurationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        countThreadsAsCoresEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        enableTrustedServiceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        enableBallooning = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initInfoIcons(ApplicationResources resources, ApplicationConstants constants, ApplicationTemplates templates)
    {
        memoryOptimizationInfo = new InfoIcon(templates.italicFixedWidth("465px", constants.clusterPopupMemoryOptimizationInfo()), resources); //$NON-NLS-1$

        cpuThreadsInfo = new InfoIcon(templates.italicFixedWidth("600px", constants.clusterPopupCpuThreadsInfo()), resources); //$NON-NLS-1$
    }

    private void applyModeCustomizations() {
        if (ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly)
        {
            optimizationTab.setVisible(false);
            resiliencePolicyTab.setVisible(false);
            clusterPolicyTab.setVisible(false);
            dataCenterPanel.addStyleName(style.generalTabTopDecoratorEmpty());
        }
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final ClusterModel object) {
        driver.edit(object);

        servicesCheckboxPanel.setVisible(object.getAllowClusterWithVirtGlusterEnabled());
        servicesRadioPanel.setVisible(!object.getAllowClusterWithVirtGlusterEnabled());

        optimizationForServerFormatter(object);
        optimizationForDesktopFormatter(object);
        optimizationCustomFormatter(object);

        object.getOptimizationForServer().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForServerFormatter(object);
            }
        });

        object.getOptimizationForDesktop().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForDesktopFormatter(object);
            }
        });

        object.getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) object.getOptimizationCustom_IsSelected().getEntity()) {
                    optimizationCustomFormatter(object);
                    optimizationCustomEditor.setVisible(true);
                }
            }
        });

        object.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                resiliencePolicyTab.setVisible(object.getisResiliencePolicyTabAvailable());
                applyModeCustomizations();
            }
        });

        object.getEnableGlusterService().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                importGlusterExplanationLabel.setVisible((Boolean) object.getEnableGlusterService().getEntity()
                        && object.getIsNew());
            }
        });
        importGlusterExplanationLabel.setVisible((Boolean) object.getEnableGlusterService().getEntity()
                && object.getIsNew());

        object.getVersionSupportsCpuThreads().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                cpuThreadsPanel.setVisible((Boolean) object.getVersionSupportsCpuThreads().getEntity());
            }
        });

        object.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
            }
        });
    }

    private void optimizationForServerFormatter(ClusterModel object) {
        if (object.getOptimizationForServer() != null
                && object.getOptimizationForServer().getEntity() != null) {
            optimizationForServerEditor.setLabel(messages.clusterPopupMemoryOptimizationForServerLabel(
                    object.getOptimizationForServer().getEntity().toString()));
        }
    }

    private void optimizationForDesktopFormatter(ClusterModel object) {
        if (object.getOptimizationForDesktop() != null
                && object.getOptimizationForDesktop().getEntity() != null) {
            optimizationForDesktopEditor.setLabel(messages.clusterPopupMemoryOptimizationForDesktopLabel(
                    object.getOptimizationForDesktop().getEntity().toString()));
        }
    }

    private void optimizationCustomFormatter(ClusterModel object) {
        if (object.getOptimizationCustom() != null
                && object.getOptimizationCustom().getEntity() != null) {
            // Use current value because object.getOptimizationCustom.getEntity() can be null
            optimizationCustomEditor.setLabel(messages.clusterPopupMemoryOptimizationCustomLabel(
                    String.valueOf(object.getMemoryOverCommit())));
        }
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    @Override
    public ClusterModel flush() {
        return driver.flush();
    }

    @Override
    public void allowClusterWithVirtGlusterEnabled(boolean value) {
        servicesCheckboxPanel.setVisible(value);
        servicesRadioPanel.setVisible(!value);
    }

    interface WidgetStyle extends CssResource {
        String label();

        String generalTabTopDecoratorEmpty();

        String editorContentWidget();

        String fullWidth();

        String timeTextBoxEditorWidget();
    }

}
