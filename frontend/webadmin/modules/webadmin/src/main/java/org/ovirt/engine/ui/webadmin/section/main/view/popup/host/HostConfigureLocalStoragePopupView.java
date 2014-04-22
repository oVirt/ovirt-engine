package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.compat.Version;
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
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ConfigureLocalStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class HostConfigureLocalStoragePopupView extends AbstractTabbedModelBoundPopupView<ConfigureLocalStorageModel>
    implements ConfigureLocalStoragePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfigureLocalStorageModel, HostConfigureLocalStoragePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostConfigureLocalStoragePopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostConfigureLocalStoragePopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private final ApplicationConstants constants;

    private ApplicationConstants getConstants() {
        return constants;
    }

    private final ApplicationMessages messages;

    private ApplicationMessages getMessages() {
        return messages;
    }

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
    FlowPanel cpuThreadsPanel;

    @UiField
    @Ignore
    Label cpuThreadsPanelTitle;

    @UiField(provided = true)
    InfoIcon cpuThreadsInfo;

    @UiField(provided = true)
    @Path(value = "cluster.countThreadsAsCores.entity")
    @WithElementId
    EntityModelCheckBoxEditor countThreadsAsCoresEditor;

    @Inject
    public HostConfigureLocalStoragePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants,
            ApplicationMessages messages, ApplicationTemplates templates) {
        super(eventBus, resources);

        this.constants = constants;
        this.messages = messages;

        initialize(resources, templates);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        localize();
        driver.initialize(this);


        // Data center edit button.
        dataCenterPanel.setVisible(false);

        dataCenterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {

                dataCenterPanel.setVisible(!dataCenterPanel.isVisible());

                dataCenterButton.setText(dataCenterPanel.isVisible() ? getConstants().closeText() : getConstants().editText());
                dataCenterNameEditor.setEnabled(dataCenterPanel.isVisible());
            }
        });


        // Cluster edit button.
        clusterPanel.setVisible(false);

        clusterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {

                clusterPanel.setVisible(!clusterPanel.isVisible());

                clusterButton.setText(clusterPanel.isVisible() ? getConstants().closeText() : getConstants().editText());
                clusterNameEditor.setEnabled(clusterPanel.isVisible());
            }
        });


        // Storage edit button.
        storageButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {

                storageNameEditor.setEnabled(!storageNameEditor.isEnabled());

                storageButton.setText(storageNameEditor.isEnabled() ? getConstants().closeText() : getConstants().editText());
            }
        });
    }

    private void initialize(ApplicationResources resources, ApplicationTemplates templates) {

        dataCenterVersionEditor = new ListModelListBoxEditor<Version>(new NullSafeRenderer<Version>() {
            @Override
            public String renderNullSafe(Version object) {
                return object.getValue();
            }
        });

        clusterCpuTypeEditor = new ListModelListBoxEditor<ServerCpu>(new NullSafeRenderer<ServerCpu>() {
            @Override
            public String renderNullSafe(ServerCpu object) {
                return object.getCpuName();
            }
        });


        // Optimization options.
        optimizationNoneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        optimizationCustomEditor.setVisible(false);

        countThreadsAsCoresEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        memoryOptimizationInfo = new InfoIcon(templates.italicFixedWidth("465px", getConstants().clusterPopupMemoryOptimizationInfo()), resources); //$NON-NLS-1$
        cpuThreadsInfo = new InfoIcon(templates.italicFixedWidth("600px", getConstants().clusterPopupCpuThreadsInfo()), resources); //$NON-NLS-1$

    }

    private void addStyles() {
        optimizationNoneEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationForServerEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationForDesktopEditor.setContentWidgetStyleName(style.fullWidth());
        optimizationCustomEditor.setContentWidgetStyleName(style.fullWidth());

        countThreadsAsCoresEditor.setContentWidgetStyleName(style.fullWidth());
    }

    void localize() {

        generalTab.setLabel(getConstants().hostPopupGeneralTabLabel());

        dataCenterButton.setText(getConstants().editText());
        dataCenterNameEditor.setLabel(getConstants().nameLabel());
        dataCenterDescriptionEditor.setLabel(getConstants().descriptionLabel());
        dataCenterVersionEditor.setLabel(getConstants().dataCenterPopupVersionLabel());

        clusterButton.setText(getConstants().editText());
        clusterNameEditor.setLabel(getConstants().clusterPopupNameLabel());
        clusterDescriptionEditor.setLabel(getConstants().clusterPopupDescriptionLabel());
        clusterCpuTypeEditor.setLabel(getConstants().clusterPopupCPUTypeLabel());

        storageButton.setText(getConstants().editText());
        storageNameEditor.setLabel(getConstants().storagePopupNameLabel());

        pathEditor.setLabel(getConstants().configureLocalStoragePopupPathLabel());

        optimizationTab.setLabel(getConstants().clusterPopupOptimizationTabLabel());

        memoryOptimizationPanelTitle.setText(getConstants().clusterPopupMemoryOptimizationPanelTitle());
        optimizationNoneEditor.setLabel(getConstants().clusterPopupOptimizationNoneLabel());

        cpuThreadsPanelTitle.setText(getConstants().clusterPopupCpuThreadsPanelTitle());
        countThreadsAsCoresEditor.setLabel(getConstants().clusterPopupCountThreadsAsCoresLabel());
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

        model.getCluster().getOptimizationForServer().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForServerFormatter(model);
            }
        });

        model.getCluster().getOptimizationForDesktop().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForDesktopFormatter(model);
            }
        });

        model.getCluster().getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (model.getCluster().getOptimizationCustom_IsSelected().getEntity()) {
                    optimizationCustomFormatter(model);
                    optimizationCustomEditor.setVisible(true);
                }
            }
        });

        model.getCluster().getVersionSupportsCpuThreads().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                cpuThreadsPanel.setVisible(model.getCluster().getVersionSupportsCpuThreads().getEntity());
            }
        });
    }

    private void optimizationForDesktopFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationForDesktop() != null
                && model.getCluster().getOptimizationForDesktop().getEntity() != null) {
            optimizationForDesktopEditor.setLabel(getMessages().clusterPopupMemoryOptimizationForDesktopLabel(
                    model.getCluster().getOptimizationForDesktop().getEntity().toString()));
        }
    }

    private void optimizationForServerFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationForServer() != null
                && model.getCluster().getOptimizationForServer().getEntity() != null) {
            optimizationForServerEditor.setLabel(getMessages().clusterPopupMemoryOptimizationForServerLabel(
                    model.getCluster().getOptimizationForServer().getEntity().toString()));
        }
    }

    private void optimizationCustomFormatter(ConfigureLocalStorageModel model) {
        if (model.getCluster() != null && model.getCluster().getOptimizationCustom() != null
                && model.getCluster().getOptimizationCustom().getEntity() != null) {
            // Use current value because object.getOptimizationCustom.getEntity() can be null
            optimizationCustomEditor.setLabel(getMessages().clusterPopupMemoryOptimizationCustomLabel(
                    String.valueOf(model.getCluster().getMemoryOverCommit())));
        }
    }

    @Override
    public ConfigureLocalStorageModel flush() {
        return driver.flush();
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
        getTabNameMapping().put(TabName.GENERAL_TAB, generalTab);
        getTabNameMapping().put(TabName.OPTIMIZATION_TAB,
                optimizationTab);
    }

}
