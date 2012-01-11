package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ConfigureLocalStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;

public class HostConfigureLocalStoragePopupView extends WebAdminModelBoundPopupView<ConfigureLocalStorageModel> implements ConfigureLocalStoragePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfigureLocalStorageModel, HostConfigureLocalStoragePopupView> {

        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostConfigureLocalStoragePopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostConfigureLocalStoragePopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    private ApplicationConstants getConstants() {
        return constants;
    }


    @UiField
    DialogTab generalTab;

    @UiField
    VerticalPanel dataCenterPanel;

    @UiField
    Anchor dataCenterButton;

    @UiField
    @Path(value = "dataCenter.name.entity")
    EntityModelTextBoxEditor dataCenterNameEditor;

    @UiField
    @Path(value = "dataCenter.description.entity")
    EntityModelTextBoxEditor dataCenterDescriptionEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> dataCenterVersionEditor;

    @UiField
    VerticalPanel clusterPanel;

    @UiField
    Anchor clusterButton;

    @UiField
    @Path(value = "cluster.name.entity")
    EntityModelTextBoxEditor clusterNameEditor;

    @UiField
    @Path(value = "cluster.description.entity")
    EntityModelTextBoxEditor clusterDescriptionEditor;

    @UiField(provided = true)
    @Path(value = "cluster.cPU.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> clusterCpuNameEditor;

    @UiField
    Anchor storageButton;

    @UiField
    @Path(value = "formattedStorageName.entity")
    EntityModelTextBoxEditor storageNameEditor;

    @UiField
    @Ignore
    Label pathLabel;

    @UiField
    @Path(value = "storage.path.entity")
    EntityModelTextBoxEditor pathEditor;

    @UiField
    @WithElementId
    DialogTab memoryOptimizationTab;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationNone_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationNoneEditor;

    @UiField
    @Ignore
    Label optimizationNoneExplanationLabel;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationForServer_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForServerEditor;

    @UiField
    @Ignore
    Label optimizationForServerExplanationLabel;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationForDesktop_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForDesktopEditor;

    @UiField
    @Ignore
    Label optimizationForDesktopExplanationLabel;

    @UiField(provided = true)
    @Path(value = "cluster.optimizationCustom_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationCustomEditor;

    @UiField(provided = true)
    @Ignore
    Label optimizationCustomExplanationLabel;


    @Inject
    public HostConfigureLocalStoragePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        this.constants = constants;

        initialize();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        Driver.driver.initialize(this);


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

    private void initialize() {

        dataCenterVersionEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Version) object).getValue();
            }
        });

        clusterCpuNameEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((ServerCpu) object).getCpuName();
            }
        });


        // Optimization options.
        optimizationNoneEditor = new EntityModelRadioButtonEditor("1");
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1");
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1");
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1");

        optimizationCustomExplanationLabel = new Label();
        optimizationCustomExplanationLabel.setVisible(false);
    }

    void localize() {

        generalTab.setLabel(getConstants().hostPopupGeneralTabLabel());
        memoryOptimizationTab.setLabel(getConstants().hostPopupMemoryOptimizationTabLabel());

        dataCenterButton.setText(getConstants().editText());
        dataCenterNameEditor.setLabel(getConstants().dataCenterPopupNameLabel());
        dataCenterDescriptionEditor.setLabel(getConstants().dataCenterPopupDescriptionLabel());
        dataCenterVersionEditor.setLabel(getConstants().dataCenterPopupVersionLabel());

        clusterButton.setText(getConstants().editText());
        clusterNameEditor.setLabel(getConstants().clusterPopupNameLabel());
        clusterDescriptionEditor.setLabel(getConstants().clusterPopupDescriptionLabel());
        clusterCpuNameEditor.setLabel(getConstants().clusterPopupCPULabel());

        storageButton.setText(getConstants().editText());
        storageNameEditor.setLabel(getConstants().storagePopupNameLabel());

        pathLabel.setText(getConstants().configureLocalStoragePopupPathLabel());

        memoryOptimizationTab.setLabel(constants.clusterPopupMemoryOptimizationTabLabel());

        optimizationNoneEditor.setLabel(constants.clusterPopupOptimizationNoneLabel());
        optimizationForServerEditor.setLabel(constants.clusterPopupOptimizationForServerLabel());
        optimizationForDesktopEditor.setLabel(constants.clusterPopupOptimizationForDesktopLabel());
        optimizationCustomEditor.setLabel(constants.clusterPopupOptimizationCustomLabel());

        optimizationNoneExplanationLabel.setText(constants.clusterPopupOptimizationNoneExplainationLabel());
        optimizationForServerExplanationLabel.setText(constants.clusterPopupOptimizationForServerExplainationLabel());
        optimizationForDesktopExplanationLabel.setText(constants.clusterPopupOptimizationForDesktopExplainationLabel());
        optimizationCustomExplanationLabel.setText(constants.clusterPopupOptimizationCustomExplainationLabel());
    }

    @Override
    public void edit(final ConfigureLocalStorageModel model) {
        Driver.driver.edit(model);

        dataCenterNameEditor.setEnabled(false);
        clusterNameEditor.setEnabled(false);
        storageNameEditor.setEnabled(false);


        model.getCluster().getOptimizationForServer().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForServerExplanationLabel.setText(StringFormat.format(optimizationForServerExplanationLabel.getText(),
                    model.getCluster().getOptimizationForServer().getEntity().toString() + "%"));
            }
        });

        model.getCluster().getOptimizationForDesktop().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForDesktopExplanationLabel.setText(StringFormat.format(optimizationForDesktopExplanationLabel.getText(),
                    model.getCluster().getOptimizationForDesktop().getEntity().toString() + "%"));
            }
        });

        model.getCluster().getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) model.getCluster().getOptimizationCustom_IsSelected().getEntity()) {
                    optimizationCustomExplanationLabel.setText(StringFormat.format(optimizationCustomExplanationLabel.getText(),
                        model.getCluster().getOptimizationCustom().getEntity().toString() + "%"));
                    optimizationCustomExplanationLabel.setVisible(true);
                }
            }
        });
    }

    @Override
    public ConfigureLocalStorageModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
    }
}
