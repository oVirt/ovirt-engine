package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.form.Slider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPolicyPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabClusterPolicyView extends AbstractSubTabFormView<VDSGroup, ClusterListModel, ClusterPolicyModel>
        implements SubTabClusterPolicyPresenter.ViewDef, Editor<ClusterPolicyModel> {

    private static final String MAX_COLOR = "#4E9FDD"; //$NON-NLS-1$
    private static final String MIN_COLOR = "#AFBF27"; //$NON-NLS-1$

    interface Driver extends SimpleBeanEditorDriver<ClusterPolicyModel, SubTabClusterPolicyView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabClusterPolicyView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private Driver driver;

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

    private final ApplicationConstants constants;

    @Inject
    public SubTabClusterPolicyView(DetailModelProvider<ClusterListModel, ClusterPolicyModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;

        initSliders();
        initLabels();
        initButton();
        initDummyPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        localize(constants);
        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        minServiceLevelLabel.setText(constants.clusterPolicyMinServiceLevelLabel());
        maxServiceLevelLabel.setText(constants.clusterPolicyMaxServiceLevelLabel());
        editPolicyButton.setLabel(constants.clusterPolicyEditPolicyButtonLabel());
        policyLabel.setText(constants.clusterPolicyPolicyLabel());
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

    @Override
    public void setMainTabSelectedItem(VDSGroup selectedItem) {
        driver.edit(getDetailModel());
        if (selectedItem.supportsVirtService())
        {
            if (selectedItem.getselection_algorithm().equals(VdsSelectionAlgorithm.PowerSave)) {
                setVisibility(true);
                leftSlider.setValue(selectedItem.getlow_utilization());
                rightSlider.setValue(selectedItem.gethigh_utilization());
                policyTimeLabel.setText(constants.clusterPolicyForTimeLabel()
                        + " " //$NON-NLS-1$
                        + selectedItem.getcpu_over_commit_duration_minutes()
                        + " " + constants.clusterPolicyMinTimeLabel()); //$NON-NLS-1$
                policyFieldLabel.setText(constants.clusterPolicyPowSaveLabel());
            } else if (selectedItem.getselection_algorithm().equals(VdsSelectionAlgorithm.EvenlyDistribute)) {
                setVisibility(true);
                leftSlider.setVisible(false);
                leftDummySlider.setVisible(true);
                rightSlider.setValue(selectedItem.gethigh_utilization());
                policyTimeLabel.setText(constants.clusterPolicyForTimeLabel()
                        + " " //$NON-NLS-1$
                        + selectedItem.getcpu_over_commit_duration_minutes()
                        + " " + constants.clusterPolicyMinTimeLabel()); //$NON-NLS-1$
                policyFieldLabel.setText(constants.clusterPolicyEvenDistLabel());
            } else { // also for VdsSelectionAlgorithm.None
                setVisibility(false);
                policyFieldLabel.setText(constants.clusterPolicyNoneLabel());
            }
        }
    }
}
