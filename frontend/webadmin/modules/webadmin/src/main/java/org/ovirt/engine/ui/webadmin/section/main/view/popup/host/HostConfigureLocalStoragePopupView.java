package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ConfigureLocalStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class HostConfigureLocalStoragePopupView extends AbstractTabbedModelBoundPopupView<ConfigureLocalStorageModel>
    implements ConfigureLocalStoragePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ConfigureLocalStorageModel, HostConfigureLocalStoragePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostConfigureLocalStoragePopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostConfigureLocalStoragePopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    WidgetStyle style;

    @UiField
    DialogTab generalTab;

    @UiField
    VerticalPanel dataCenterPanel;

    @UiField
    Anchor dataCenterButton;

    @UiField
    @Path(value = "dataCenter.name.entity")
    StringEntityModelTextBoxEditor dataCenterNameEditor;

    @UiField
    @Path(value = "dataCenter.description.entity")
    StringEntityModelTextBoxEditor dataCenterDescriptionEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Version> dataCenterVersionEditor;

    @UiField
    VerticalPanel clusterPanel;

    @UiField
    Anchor clusterButton;

    @UiField
    @Path(value = "cluster.name.entity")
    StringEntityModelTextBoxEditor clusterNameEditor;

    @UiField
    @Path(value = "cluster.description.entity")
    StringEntityModelTextBoxEditor clusterDescriptionEditor;

    @UiField(provided = true)
    @Path(value = "cluster.CPU.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ServerCpu> clusterCpuTypeEditor;

    @UiField
    Anchor storageButton;

    @UiField
    @Path(value = "formattedStorageName.entity")
    StringEntityModelTextBoxEditor storageNameEditor;

    @UiField
    @Path(value = "storage.path.entity")
    StringEntityModelTextBoxEditor pathEditor;

    @UiField
    @WithElementId
    DialogTab optimizationTab;

    @UiField
    @Ignore
    Label memoryOptimizationPanelTitle;

    @UiField(provided = true)
    InfoIcon memoryOptimizationInfo;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationNone_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationNoneEditor;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationForServer_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForServerEditor;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationForDesktop_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForDesktopEditor;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationCustom_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationCustomEditor;

    @UiField
    @Ignore
    Label cpuThreadsPanelTitle;

    @UiField(provided = true)
    InfoIcon cpuThreadsInfo;

    @UiField(provided = true)
    @Path(value = "cluster.countThreadsAsCores.entity")
    @WithElementId
    EntityModelCheckBoxEditor countThreadsAsCoresEditor;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public HostConfigureLocalStoragePopupView(EventBus eventBus) {
        super(eventBus);

        initialize();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        localize();
        driver.initialize(this);


        // Data center edit button.
        dataCenterPanel.setVisible(false);

        dataCenterButton.addClickHandler(clickEvent -> {

            dataCenterPanel.setVisible(!dataCenterPanel.isVisible());

            dataCenterButton.setText(dataCenterPanel.isVisible() ? constants.closeText() : constants.editText());
            dataCenterNameEditor.setEnabled(dataCenterPanel.isVisible());
        });


        // Cluster edit button.
        clusterPanel.setVisible(false);

        clusterButton.addClickHandler(clickEvent -> {

            clusterPanel.setVisible(!clusterPanel.isVisible());

            clusterButton.setText(clusterPanel.isVisible() ? constants.closeText() : constants.editText());
            clusterNameEditor.setEnabled(clusterPanel.isVisible());
        });


        // Storage edit button.
        storageButton.addClickHandler(clickEvent -> {

            storageNameEditor.setEnabled(!storageNameEditor.isEnabled());

            storageButton.setText(storageNameEditor.isEnabled() ? constants.closeText() : constants.editText());
        });
    }

    private void initialize() {

        dataCenterVersionEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Version>() {
            @Override
            public String renderNullSafe(Version object) {
                return object.getValue();
            }
        });

        clusterCpuTypeEditor = new ListModelListBoxEditor<>(new AbstractRenderer<ServerCpu>() {
            @Override
            public String render(ServerCpu object) {
                return object != null  && object.getCpuName().length() > 0
                        ?
                        object.getCpuName()
                        :
                        constants.autoDetect();
            }
        });

        // Optimization options.
        optimizationNoneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        optimizationCustomEditor.setVisible(false);

        countThreadsAsCoresEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        memoryOptimizationInfo = new InfoIcon(templates.italicText(constants.clusterPopupMemoryOptimizationInfo()));
        memoryOptimizationInfo.setTooltipMaxWidth(TooltipWidth.W520);
        cpuThreadsInfo = new InfoIcon(templates.italicText(constants.clusterPopupCpuThreadsInfo()));
        cpuThreadsInfo.setTooltipMaxWidth(TooltipWidth.W620);

    }

    private void addStyles() {
        optimizationNoneEditor.setContentWidgetContainerStyleName(style.fullWidth());
        optimizationForServerEditor.setContentWidgetContainerStyleName(style.fullWidth());
        optimizationForDesktopEditor.setContentWidgetContainerStyleName(style.fullWidth());
        optimizationCustomEditor.setContentWidgetContainerStyleName(style.fullWidth());

        countThreadsAsCoresEditor.setContentWidgetContainerStyleName(style.fullWidth());
    }

    void localize() {

        generalTab.setLabel(constants.hostPopupGeneralTabLabel());

        dataCenterButton.setText(constants.editText());
        dataCenterNameEditor.setLabel(constants.nameLabel());
        dataCenterDescriptionEditor.setLabel(constants.descriptionLabel());
        dataCenterVersionEditor.setLabel(constants.dataCenterPopupVersionLabel());

        clusterButton.setText(constants.editText());
        clusterNameEditor.setLabel(constants.clusterPopupNameLabel());
        clusterDescriptionEditor.setLabel(constants.clusterPopupDescriptionLabel());
        clusterCpuTypeEditor.setLabel(constants.clusterPopupCPUTypeLabel());

        storageButton.setText(constants.editText());
        storageNameEditor.setLabel(constants.storagePopupNameLabel());

        pathEditor.setLabel(constants.configureLocalStoragePopupPathLabel());

        optimizationTab.setLabel(constants.clusterPopupOptimizationTabLabel());

        memoryOptimizationPanelTitle.setText(constants.clusterPopupMemoryOptimizationPanelTitle());
        optimizationNoneEditor.setLabel(constants.clusterPopupOptimizationNoneLabel());

        cpuThreadsPanelTitle.setText(constants.clusterPopupCpuThreadsPanelTitle());
        countThreadsAsCoresEditor.setLabel(constants.clusterPopupCountThreadsAsCoresLabel());
    }

    @Override
    public void edit(final ConfigureLocalStorageModel model) {
        driver.edit(model);

        dataCenterNameEditor.setEnabled(false);
        clusterNameEditor.setEnabled(false);
        storageNameEditor.setEnabled(false);

        optimizationForServerFormatter(model);
        optimizationForDesktopFormatter(model);
        optimizationCustomFormatter(model);

        model.getCluster().getOptimizationForServer().getEntityChangedEvent().addListener((ev, sender, args) -> optimizationForServerFormatter(model));

        model.getCluster().getOptimizationForDesktop().getEntityChangedEvent().addListener((ev, sender, args) -> optimizationForDesktopFormatter(model));

        model.getCluster().getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (model.getCluster().getOptimizationCustom_IsSelected().getEntity()) {
                optimizationCustomFormatter(model);
                optimizationCustomEditor.setVisible(true);
            }
        });

    }

    private void optimizationForDesktopFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationForDesktop() != null
                && model.getCluster().getOptimizationForDesktop().getEntity() != null) {
            optimizationForDesktopEditor.setLabel(messages.clusterPopupMemoryOptimizationForDesktopLabel(
                    model.getCluster().getOptimizationForDesktop().getEntity().toString()));
        }
    }

    private void optimizationForServerFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationForServer() != null
                && model.getCluster().getOptimizationForServer().getEntity() != null) {
            optimizationForServerEditor.setLabel(messages.clusterPopupMemoryOptimizationForServerLabel(
                    model.getCluster().getOptimizationForServer().getEntity().toString()));
        }
    }

    private void optimizationCustomFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationCustom() != null
                && model.getCluster().getOptimizationCustom().getEntity() != null) {
            // Use current value because object.getOptimizationCustom.getEntity() can be null
            optimizationCustomEditor.setLabel(messages.clusterPopupMemoryOptimizationCustomLabel(
                    String.valueOf(model.getCluster().getMemoryOverCommit())));
        }
    }

    @Override
    public ConfigureLocalStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
    }

    interface WidgetStyle extends CssResource {
        String fullWidth();
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, generalTab.getTabListItem());
        getTabNameMapping().put(TabName.OPTIMIZATION_TAB,
                optimizationTab.getTabListItem());
    }

}
