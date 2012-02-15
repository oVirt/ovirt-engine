package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.Slider;
import org.ovirt.engine.ui.common.widget.form.Slider.SliderValueChange;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class ClusterPolicyPopupView extends WebAdminModelBoundPopupView<ClusterPolicyModel> implements ClusterPolicyPopupPresenterWidget.ViewDef, SliderValueChange {

    private static final String RIGHT = "right";

    private static final String LEFT = "left";

    private static final String MAX_COLOR = "#4E9FDD";

    private static final String MIN_COLOR = "#AFBF27";

    interface Driver extends SimpleBeanEditorDriver<ClusterPolicyModel, ClusterPolicyPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterPolicyPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    Slider leftSlider;

    @UiField(provided = true)
    Slider rightSlider;

    @UiField(provided = true)
    @Ignore
    Label maxServiceLevelLabel;

    @UiField(provided = true)
    @Ignore
    Label minServiceLevelLabel;

    @UiField(provided = true)
    @Ignore
    SimplePanel leftDummySlider;

    @UiField(provided = true)
    @Ignore
    SimplePanel rightDummySlider;

    @UiField(provided = true)
    @Ignore
    RadioButton policyRadioButton_none;

    @UiField(provided = true)
    @Ignore
    RadioButton policyRadioButton_evenDist;

    @UiField(provided = true)
    @Ignore
    RadioButton policyRadioButton_powerSave;

    @UiField(provided = true)
    @Path(value = "overCommitTime.entity")
    EntityModelTextBoxEditor overCommitTimeEditor;

    @UiField
    @Ignore
    HorizontalPanel timeHorizontalPanel;

    @UiField
    @Ignore
    Label forTimeLabel;

    @UiField
    @Ignore
    Label minTimeLabel;

    private ClusterPolicyModel clusterPolicyModel;

    public ClusterPolicyModel getClusterPolicyModel() {
        return clusterPolicyModel;
    }

    public void setClusterPolicyModel(ClusterPolicyModel entity) {
        this.clusterPolicyModel = entity;
    }

    @Inject
    public ClusterPolicyPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initSliders();
        initLabels();
        initRadioButtons();
        initDummyPanel();
        initTextBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        policyRadioButton_none.setText(constants.clusterPolicyNoneLabel());
        policyRadioButton_evenDist.setText(constants.clusterPolicyEvenDistLabel());
        policyRadioButton_powerSave.setText(constants.clusterPolicyPowSaveLabel());
        maxServiceLevelLabel.setText(constants.clusterPolicyMaxServiceLevelLabel());
        minServiceLevelLabel.setText(constants.clusterPolicyMinServiceLevelLabel());
        forTimeLabel.setText(constants.clusterPolicyForTimeLabel());
        minTimeLabel.setText(constants.clusterPolicyMinTimeLabel());
    }

    private void addStyles() {
        overCommitTimeEditor.addContentWidgetStyleName(style.timeTextBoxEditorWidget());
    }

    private void initTextBox() {
        overCommitTimeEditor = new EntityModelTextBoxEditor();
    }

    private void initRadioButtons() {
        policyRadioButton_none = new RadioButton("policyRadioButtonGroup", "");
        policyRadioButton_evenDist = new RadioButton("policyRadioButtonGroup", "");
        policyRadioButton_powerSave = new RadioButton("policyRadioButtonGroup", "");

        policyRadioButton_none.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    getClusterPolicyModel().setSelectionAlgorithm(VdsSelectionAlgorithm.None);
                }
                setSelectionAlgorithm();
            }
        });

        policyRadioButton_evenDist.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    getClusterPolicyModel().setSelectionAlgorithm(VdsSelectionAlgorithm.EvenlyDistribute);
                }
                setSelectionAlgorithm();
            }
        });

        policyRadioButton_powerSave.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    getClusterPolicyModel().setSelectionAlgorithm(VdsSelectionAlgorithm.PowerSave);
                }
                setSelectionAlgorithm();
            }
        });

    }

    private void initDummyPanel() {
        leftDummySlider = new SimplePanel();
        leftDummySlider.setVisible(false);
        rightDummySlider = new SimplePanel();
        rightDummySlider.setVisible(false);
    }

    private void initLabels() {
        maxServiceLevelLabel = new Label();
        minServiceLevelLabel = new Label();
    }

    private void initSliders() {
        leftSlider = new Slider(4, 10, 50, 20, MIN_COLOR);
        leftSlider.setSliderValueChange(LEFT, this);
        rightSlider = new Slider(4, 51, 90, 75, MAX_COLOR);
        rightSlider.setSliderValueChange(RIGHT, this);
    }

    private void setVisibility(boolean b) {
        rightSlider.setVisible(b);
        leftSlider.setVisible(b);
        leftDummySlider.setVisible(false);
        rightDummySlider.setVisible(false);
        timeHorizontalPanel.setVisible(b);
    }

    @Override
    public void edit(ClusterPolicyModel object) {
        Driver.driver.edit(object);
        setClusterPolicyModel(object);
        if (getClusterPolicyModel().getSelectionAlgorithm().equals(VdsSelectionAlgorithm.PowerSave)) {
            policyRadioButton_powerSave.setValue(true);
        } else if (getClusterPolicyModel().getSelectionAlgorithm()
                .equals(VdsSelectionAlgorithm.EvenlyDistribute)) {
            policyRadioButton_evenDist.setValue(true);
        } else {
            policyRadioButton_none.setValue(true);
        }

        setSelectionAlgorithm();
    }

    private void setSelectionAlgorithm() {
        if (getClusterPolicyModel().getSelectionAlgorithm().equals(VdsSelectionAlgorithm.PowerSave)) {
            setVisibility(true);
            leftSlider.setValue((getClusterPolicyModel().getOverCommitLowLevel() < 10 ? 20
                    : getClusterPolicyModel().getOverCommitLowLevel()));
            if (getClusterPolicyModel().getOverCommitHighLevel() <= 50
                    || getClusterPolicyModel().getOverCommitHighLevel() > 90) {
                rightSlider.setValue(75);
            } else {
                rightSlider.setValue(getClusterPolicyModel().getOverCommitHighLevel());
            }
            // timeTextBox.setText(getClusterPolicyModel().getOverCommitTime().getEntity() + "");
        } else if (getClusterPolicyModel().getSelectionAlgorithm()
                .equals(VdsSelectionAlgorithm.EvenlyDistribute)) {
            setVisibility(true);
            leftSlider.setVisible(false);
            leftDummySlider.setVisible(true);
            if (getClusterPolicyModel().getOverCommitHighLevel() <= 50
                    || getClusterPolicyModel().getOverCommitHighLevel() >= 90) {
                rightSlider.setValue(75);
            } else {
                rightSlider.setValue(getClusterPolicyModel().getOverCommitHighLevel());
            }
            // timeTextBox.setText(getClusterPolicyModel().getOverCommitTime().getEntity() + "");
        } else { // also for VdsSelectionAlgorithm.None

            setVisibility(false);
            leftDummySlider.setVisible(true);
            rightDummySlider.setVisible(true);
        }
    }

    @Override
    public ClusterPolicyModel flush() {
        return null;
    }

    @Override
    public void onSliderValueChange(String name, int value) {
        if (name.equals(RIGHT)) {
            getClusterPolicyModel().setOverCommitHighLevel(value);
        } else if (name.equals(LEFT)) {
            getClusterPolicyModel().setOverCommitLowLevel(value);
        }
    }

    interface WidgetStyle extends CssResource {
        String timeTextBoxEditorWidget();
    }

}
