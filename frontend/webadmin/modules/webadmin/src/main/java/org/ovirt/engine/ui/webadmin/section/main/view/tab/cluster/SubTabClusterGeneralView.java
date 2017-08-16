package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabClusterGeneralView extends AbstractSubTabFormView<Cluster, ClusterListModel<Void>, ClusterGeneralModel>
        implements SubTabClusterGeneralPresenter.ViewDef, Editor<ClusterGeneralModel> {

    interface Driver extends UiCommonEditorDriver<ClusterGeneralModel, SubTabClusterGeneralView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabClusterGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    ClusterGeneralModelForm form;

    FormBuilder formBuilder;

    @UiField
    HorizontalPanel glusterSwiftPanel;

    @UiField(provided = true)
    EntityModelLabelEditor<GlusterServiceStatus> glusterSwiftStatusEditor;

    @UiField
    UiCommandButton manageGlusterSwiftButton;

    @UiField
    HTMLPanel alertsPanel;

    // This is the list of action items inside the panel, so that we
    // can clear and add elements inside without affecting the panel:
    @UiField
    FlowPanel alertsList;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterGeneralView(final DetailModelProvider<ClusterListModel<Void>, ClusterGeneralModel> modelProvider) {
        super(modelProvider);

        // Inject a reference to the resources:

        this.form = new ClusterGeneralModelForm(modelProvider);

        // generate ids
        generateIds();

        // init form
        form.initialize();

        glusterSwiftStatusEditor = new EntityModelLabelEditor<>(new EnumRenderer<GlusterServiceStatus>());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        initManageGlusterSwift();
        localize();
        addStyles();

        modelProvider.getModel().getEntityChangedEvent().addListener((ev, sender, args) -> {
            Cluster entity = modelProvider.getModel().getEntity();

            if (entity != null) {
                setMainSelectedItem(entity);
            }
        });

        driver.initialize(this);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initManageGlusterSwift() {
        manageGlusterSwiftButton.setCommand(getDetailModel().getManageGlusterSwiftCommand());
        manageGlusterSwiftButton.addClickHandler(event -> manageGlusterSwiftButton.getCommand().execute());
    }

    private void localize() {
        glusterSwiftStatusEditor.setLabel(constants.clusterGlusterSwiftLabel());
        manageGlusterSwiftButton.setLabel(constants.clusterGlusterSwiftManageLabel());
    }

    private void addStyles() {
        glusterSwiftStatusEditor.addContentWidgetContainerStyleName(style.glusterSwiftStatus());
    }

    @Override
    public void setMainSelectedItem(Cluster selectedItem) {
        driver.edit(getDetailModel());
        form.update();
        glusterSwiftPanel.setVisible(false);
    }

    @Override
    public void clearAlerts() {
        // Remove all the alert widgets and make the panel invisible:
        alertsList.clear();
        alertsPanel.setVisible(false);
    }

    @Override
    public void addAlert(Widget alertWidget) {
        alertsList.add(new InLineAlertWidget(alertWidget));

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }

    interface WidgetStyle extends CssResource {
        String glusterSwiftStatus();
    }
}
