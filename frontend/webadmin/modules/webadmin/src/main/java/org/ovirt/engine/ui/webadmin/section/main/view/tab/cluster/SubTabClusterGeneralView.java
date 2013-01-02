package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.form.Slider;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabClusterGeneralView extends AbstractSubTabFormView<VDSGroup, ClusterListModel, ClusterGeneralModel>
        implements SubTabClusterGeneralPresenter.ViewDef, Editor<ClusterGeneralModel> {

    private static final String MAX_COLOR = "#4E9FDD"; //$NON-NLS-1$
    private static final String MIN_COLOR = "#AFBF27"; //$NON-NLS-1$

    interface Driver extends SimpleBeanEditorDriver<ClusterGeneralModel, SubTabClusterGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabClusterGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    // to find the icon for alert messages:
    private final ApplicationResources resources;

    @UiField
    HorizontalPanel policyPanel;

    @UiField(provided = true)
    Slider leftSlider;

    @UiField(provided = true)
    Slider rightSlider;

    @UiField(provided = true)
    UiCommandButton editPolicyButton;

    @UiField(provided = true)
    @Ignore
    Label policyLabel;

    @UiField(provided = true)
    @Ignore
    Label policyFieldLabel;

    @UiField(provided = true)
    @Ignore
    Label policyTimeLabel;

    @UiField(provided = true)
    @Ignore
    Label maxServiceLevelLabel;

    @UiField(provided = true)
    @Ignore
    Label minServiceLevelLabel;

    @UiField(provided = true)
    @Ignore
    SimplePanel leftDummySlider;

    @UiField
    @Ignore
    AbsolutePanel sliderPanel;

    @UiField
    VerticalPanel volumeSummaryPanel;

    @UiField
    GeneralFormPanel volumeFormPanel;

    FormBuilder formBuilder;

    @UiField
    @Ignore
    Label volumeHeaderLabel;

    TextBoxLabel noOfVolumesTotal = new TextBoxLabel();
    TextBoxLabel noOfVolumesUp = new TextBoxLabel();
    TextBoxLabel noOfVolumesDown = new TextBoxLabel();

    @UiField
    HTMLPanel alertsPanel;

    // This is the list of action items inside the panel, so that we
    // can clear and add elements inside without affecting the panel:
    @UiField
    FlowPanel alertsList;

    private final ApplicationConstants constants;

    @Inject
    public SubTabClusterGeneralView(final DetailModelProvider<ClusterListModel, ClusterGeneralModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;

        // Inject a reference to the resources:
        this.resources = resources;

        initSliders();
        initLabels();
        initButton();
        initDummyPanel();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        modelProvider.getModel().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                VDSGroup entity = modelProvider.getModel().getEntity();

                if (entity != null) {
                    setMainTabSelectedItem(entity);
                }
            }
        });

        localize(constants);
        Driver.driver.initialize(this);
        buildVolumeDetailsPanel();
    }

    private void localize(ApplicationConstants constants) {
        minServiceLevelLabel.setText(constants.clusterPolicyMinServiceLevelLabel());
        maxServiceLevelLabel.setText(constants.clusterPolicyMaxServiceLevelLabel());
        editPolicyButton.setLabel(constants.clusterPolicyEditPolicyButtonLabel());
        policyLabel.setText(constants.clusterPolicyPolicyLabel());
        volumeHeaderLabel.setText(constants.clusterVolumesLabel());
    }

    private void initDummyPanel() {
        leftDummySlider = new SimplePanel();
        leftDummySlider.setVisible(false);
    }

    private void initLabels() {
        policyLabel = new Label();
        policyFieldLabel = new Label();
        policyTimeLabel = new Label();
        maxServiceLevelLabel = new Label();
        minServiceLevelLabel = new Label();
    }

    private void initButton() {
        editPolicyButton = new UiCommandButton();
        editPolicyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getDetailModel().getEditPolicyCommand().Execute(Boolean.TRUE);
            }
        });
    }

    private void initSliders() {
        leftSlider = new Slider(4, 10, 50, 20, MIN_COLOR);
        rightSlider = new Slider(4, 50, 90, 75, MAX_COLOR);
    }

    private void setVisibility(boolean b) {
        rightSlider.setVisible(b);
        leftSlider.setVisible(b);
        leftDummySlider.setVisible(false);
        policyTimeLabel.setVisible(b);
        if (sliderPanel != null) {
            sliderPanel.setVisible(b);
        }
    }

    private void buildVolumeDetailsPanel() {
        formBuilder = new FormBuilder(volumeFormPanel, 1, 3);
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesTotalLabel(), noOfVolumesTotal, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesUpLabel(), noOfVolumesUp, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesDownLabel(), noOfVolumesDown, 2, 0));
    }

    @Override
    public void setMainTabSelectedItem(VDSGroup selectedItem) {
        Driver.driver.edit(getDetailModel());
        formBuilder.showForm(getDetailModel());
        if(selectedItem.supportsVirtService())
        {
            if (selectedItem.getselection_algorithm().equals(VdsSelectionAlgorithm.PowerSave)) {
                setVisibility(true);
                leftSlider.setValue(selectedItem.getlow_utilization());
                rightSlider.setValue(selectedItem.gethigh_utilization());
                policyTimeLabel.setText(constants.clusterPolicyForTimeLabel() + " " //$NON-NLS-1$
                        + selectedItem.getcpu_over_commit_duration_minutes() + " " + constants.clusterPolicyMinTimeLabel()); //$NON-NLS-1$
                policyFieldLabel.setText(constants.clusterPolicyPowSaveLabel());
            } else if (selectedItem.getselection_algorithm().equals(VdsSelectionAlgorithm.EvenlyDistribute)) {
                setVisibility(true);
                leftSlider.setVisible(false);
                leftDummySlider.setVisible(true);
                rightSlider.setValue(selectedItem.gethigh_utilization());
                policyTimeLabel.setText(constants.clusterPolicyForTimeLabel() + " " //$NON-NLS-1$
                        + selectedItem.getcpu_over_commit_duration_minutes() + " " + constants.clusterPolicyMinTimeLabel()); //$NON-NLS-1$
                policyFieldLabel.setText(constants.clusterPolicyEvenDistLabel());
            } else { // also for VdsSelectionAlgorithm.None
                setVisibility(false);
                policyFieldLabel.setText(constants.clusterPolicyNoneLabel());
            }
        }

        policyPanel.setVisible(selectedItem.supportsVirtService());
        volumeSummaryPanel.setVisible(selectedItem.supportsGlusterService());
    }

    @Override
    public void clearAlerts() {
        // Remove all the alert widgets and make the panel invisible:
        alertsList.clear();
        alertsPanel.setVisible(false);
    }

    @Override
    public void addAlert(Widget alertWidget) {
        // Create a composite panel that contains the alert icon and the widget provided
        // by the caller, both rendered horizontally:
        FlowPanel alertPanel = new FlowPanel();
        Image alertIcon = new Image(resources.alertImage());
        alertIcon.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertWidget.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(alertIcon);
        alertPanel.add(alertWidget);

        // Add the composite panel to the alerts panel:
        alertsList.add(alertPanel);

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }
}
