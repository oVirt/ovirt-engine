package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import java.text.ParseException;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterFeaturesUtil;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabClusterGeneralView extends AbstractSubTabFormView<VDSGroup, ClusterListModel, ClusterGeneralModel>
        implements SubTabClusterGeneralPresenter.ViewDef, Editor<ClusterGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<ClusterGeneralModel, SubTabClusterGeneralView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabClusterGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    // to find the icon for alert messages:
    private final ApplicationResources resources;

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Ignore
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

    private final ApplicationConstants constants;

    @Inject
    public SubTabClusterGeneralView(final DetailModelProvider<ClusterListModel, ClusterGeneralModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;

        // Inject a reference to the resources:
        this.resources = resources;
        this.form = new ClusterGeneralModelForm(modelProvider, constants);
        glusterSwiftStatusEditor = new EntityModelLabelEditor<GlusterServiceStatus>(new EnumRenderer<GlusterServiceStatus>(), new Parser<GlusterServiceStatus>() {
            @Override
            public GlusterServiceStatus parse(CharSequence text) throws ParseException {
                if (StringHelper.isNullOrEmpty(text.toString())) {
                    return null;
                }
                return GlusterServiceStatus.valueOf(text.toString().toUpperCase());
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initManageGlusterSwift();
        localize();
        addStyles();

        modelProvider.getModel().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                VDSGroup entity = modelProvider.getModel().getEntity();

                if (entity != null) {
                    setMainTabSelectedItem(entity);
                }
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
        manageGlusterSwiftButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                manageGlusterSwiftButton.getCommand().execute();
            }
        });
    }

    private void localize() {
        glusterSwiftStatusEditor.setLabel(constants.clusterGlusterSwiftLabel());
        manageGlusterSwiftButton.setLabel(constants.clusterGlusterSwiftManageLabel());
    }

    private void addStyles() {
        glusterSwiftStatusEditor.addContentWidgetStyleName(style.glusterSwiftStatus());
    }

    @Override
    public void setMainTabSelectedItem(VDSGroup selectedItem) {
        driver.edit(getDetailModel());
        form.update();
        glusterSwiftPanel.setVisible(selectedItem.supportsGlusterService()
                && GlusterFeaturesUtil.isGlusterSwiftSupported(selectedItem.getcompatibility_version()));
    }

    @Override
    public void clearAlerts() {
        // Remove all the alert widgets and make the panel invisible:
        alertsList.clear();
        alertsPanel.setVisible(false);
    }

    @Override
    public void addAlert(Widget alertWidget) {
        alertsList.add(new InLineAlertWidget(resources, alertWidget));

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }

    interface WidgetStyle extends CssResource {
        String glusterSwiftStatus();
    }
}
