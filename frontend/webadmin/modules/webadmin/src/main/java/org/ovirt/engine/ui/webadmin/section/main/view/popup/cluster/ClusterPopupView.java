package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ClusterPopupView extends AbstractModelBoundPopupView<ClusterModel> implements ClusterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterModel, ClusterPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ClusterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @WithElementId
    DialogTab generalTab;

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

    @UiField(provided = true)
    @Path(value = "cPU.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> cPUEditor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> versionEditor;

    @UiField
    @WithElementId
    DialogTab memoryOptimizationTab;

    @UiField(provided = true)
    @Path(value = "optimizationNone_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationNoneEditor;

    @UiField
    @Ignore
    Label optimizationNoneExplanationLabel;

    @UiField(provided = true)
    @Path(value = "optimizationForServer_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForServerEditor;

    @UiField
    @Ignore
    Label optimizationForServerExplanationLabel;

    @UiField(provided = true)
    @Path(value = "optimizationForDesktop_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForDesktopEditor;

    @UiField
    @Ignore
    Label optimizationForDesktopExplanationLabel;

    @UiField(provided = true)
    @Path(value = "optimizationCustom_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationCustomEditor;

    @UiField(provided = true)
    @Ignore
    Label optimizationCustomExplanationLabel;

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

    @Inject
    public ClusterPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void addStyles() {
        migrateOnErrorOption_NOEditor.addContentWidgetStyleName(style.label());
        migrateOnErrorOption_YESEditor.addContentWidgetStyleName(style.label());
        migrateOnErrorOption_HA_ONLYEditor.addContentWidgetStyleName(style.label());
    }

    private void localize(ApplicationConstants constants) {
        generalTab.setLabel(constants.clusterPopupGeneralTabLabel());

        dataCenterEditor.setLabel(constants.clusterPopupDataCenterLabel());
        nameEditor.setLabel(constants.clusterPopupNameLabel());
        descriptionEditor.setLabel(constants.clusterPopupDescriptionLabel());
        cPUEditor.setLabel(constants.clusterPopupCPULabel());
        versionEditor.setLabel(constants.clusterPopupVersionLabel());

        memoryOptimizationTab.setLabel(constants.clusterPopupMemoryOptimizationTabLabel());

        optimizationNoneEditor.setLabel(constants.clusterPopupOptimizationNoneLabel());
        optimizationForServerEditor.setLabel(constants.clusterPopupOptimizationForServerLabel());
        optimizationForDesktopEditor.setLabel(constants.clusterPopupOptimizationForDesktopLabel());
        optimizationCustomEditor.setLabel(constants.clusterPopupOptimizationCustomLabel());

        optimizationNoneExplanationLabel.setText(constants.clusterPopupOptimizationNoneExplainationLabel());
        optimizationForServerExplanationLabel.setText(constants.clusterPopupOptimizationForServerExplainationLabel());
        optimizationForDesktopExplanationLabel.setText(constants.clusterPopupOptimizationForDesktopExplainationLabel());
        optimizationCustomExplanationLabel.setText(constants.clusterPopupOptimizationCustomExplainationLabel());

        resiliencePolicyTab.setLabel(constants.clusterPopupResiliencePolicyTabLabel());

        migrateOnErrorOption_YESEditor.setLabel(constants.clusterPopupMigrateOnError_YesLabel());
        migrateOnErrorOption_HA_ONLYEditor.setLabel(constants.clusterPopupMigrateOnError_HaLabel());
        migrateOnErrorOption_NOEditor.setLabel(constants.clusterPopupMigrateOnError_NoLabel());

    }

    private void initRadioButtonEditors() {
        optimizationNoneEditor = new EntityModelRadioButtonEditor("1");
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1");
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1");
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1");

        migrateOnErrorOption_YESEditor = new EntityModelRadioButtonEditor("2");
        migrateOnErrorOption_HA_ONLYEditor = new EntityModelRadioButtonEditor("2");
        migrateOnErrorOption_NOEditor = new EntityModelRadioButtonEditor("2");

        optimizationCustomExplanationLabel = new Label();
        optimizationCustomExplanationLabel.setVisible(false);
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
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

    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final ClusterModel object) {
        Driver.driver.edit(object);

        object.getOptimizationForServer().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForServerExplanationLabel.setText(StringFormat.format(optimizationForServerExplanationLabel.getText(),
                        object.getOptimizationForServer().getEntity().toString() + "%"));
            }
        });
        object.getOptimizationForDesktop().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                optimizationForDesktopExplanationLabel.setText(StringFormat.format(optimizationForDesktopExplanationLabel.getText(),
                        object.getOptimizationForDesktop().getEntity().toString() + "%"));
            }
        });
        object.getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) object.getOptimizationCustom_IsSelected().getEntity()) {
                    optimizationCustomExplanationLabel.setText(StringFormat.format(optimizationCustomExplanationLabel.getText(),
                            object.getOptimizationCustom().getEntity().toString() + "%"));
                    optimizationCustomExplanationLabel.setVisible(true);
                }
            }
        });
        object.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                resiliencePolicyTab.setVisible(object.getisResiliencePolicyTabAvailable());
            }
        });
    }

    @Override
    public ClusterModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String label();
    }

}
