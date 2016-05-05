package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.EnumTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.MemorySizeTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralInfoPresenter;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget.AlertType;
import org.ovirt.engine.ui.webadmin.widget.label.DetailsTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberValueLabel;
import org.ovirt.engine.ui.webadmin.widget.label.PercentTextBoxLabel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabHostGeneralInfoView extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostGeneralModel>
    implements SubTabHostGeneralInfoPresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostGeneralModel, SubTabHostGeneralInfoView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGeneralInfoView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Path("IScsiInitiatorName")
    StringValueLabel iScsiInitiatorName = new StringValueLabel();
    PercentTextBoxLabel<Integer> sharedMemory = new PercentTextBoxLabel<>();
    BooleanTextBoxLabel memoryPageSharing = new BooleanTextBoxLabel(constants.active(), constants.inactive());
    NullableNumberValueLabel<Integer> activeVms = new NullableNumberValueLabel<>();
    NullableNumberValueLabel<Integer> logicalCores = new NullableNumberValueLabel<>();
    StringValueLabel onlineCores = new StringValueLabel();
    StringValueLabel spmPriority = new StringValueLabel();
    StringValueLabel hostedEngineHa = new StringValueLabel();
    FullDateTimeLabel bootTime = new FullDateTimeLabel();
    StringValueLabel kdumpStatus = new StringValueLabel();
    StringValueLabel selinuxEnforceMode = new StringValueLabel();
    StringValueLabel clusterCompatibilityVersion = new StringValueLabel();

    MemorySizeTextBoxLabel<Integer> physicalMemory = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Integer> usedMemory = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Integer> freeMemory = new MemorySizeTextBoxLabel<>();

    MemorySizeTextBoxLabel<Long> swapTotal = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Long> usedSwap = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Long> swapFree = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Float> maxSchedulingMemory = new MemorySizeTextBoxLabel<>();

    BooleanTextBoxLabel hostDevicePassthroughSupport = new BooleanTextBoxLabel(constants.enabled(), constants.disabled());

    @Ignore
    DetailsTextBoxLabel<ArrayList<ValueLabel<Integer>>, Integer> physicalMemoryDetails =
            new DetailsTextBoxLabel<>(constants.total(), constants.used(), constants.free());

    @Ignore
    DetailsTextBoxLabel<ArrayList<ValueLabel<Long>>, Long> swapSizeDetails =
            new DetailsTextBoxLabel<>(constants.total(), constants.used(), constants.free());

    @Ignore
    EnumTextBoxLabel<VdsTransparentHugePagesState> automaticLargePage = new EnumTextBoxLabel<>();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    // This is the panel containing the action items label and the
    // potential list of action items, this way we can hide the panel
    // completely (including the label) if there are no action items
    // to present:
    @UiField
    HTMLPanel alertsPanel;

    // This is the list of action items inside the panel, so that we
    // can clear and add elements inside without affecting the panel:
    @UiField
    FlowPanel alertsList;

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralInfoView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostGeneralInfoView(DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        super(modelProvider);

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setRelativeColumnWidth(0, 3);
        formBuilder.setRelativeColumnWidth(1, 4);
        formBuilder.setRelativeColumnWidth(2, 5);

        formBuilder.addFormItem(new FormItem(constants.spmPriority(), spmPriority, 0, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.activeVmsHostGeneral(), activeVms, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.logicalCores(), logicalCores, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.onlineCores(), onlineCores, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.bootTimeHostGeneral(), bootTime, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.hostedEngineHaHostGeneral(), hostedEngineHa, 0,
                virtSupported).withAutoPlacement());

        formBuilder.addFormItem(new FormItem(constants.isciInitNameHostGeneral(), iScsiInitiatorName, 0, 1,
                virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kdumpStatus(), kdumpStatus, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.physMemHostGeneral(), physicalMemoryDetails, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.swapSizeHostGeneral(), swapSizeDetails, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.sharedMemHostGeneral(), sharedMemory, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.hostDevicePassthroughHostGeneral(), hostDevicePassthroughSupport, 1).withAutoPlacement());

        formBuilder.addFormItem(new FormItem(constants.maxSchedulingMemory(), maxSchedulingMemory, 0, 2, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.memPageSharingHostGeneral(), memoryPageSharing, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.autoLargePagesHostGeneral(), automaticLargePage, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.selinuxModeGeneral(), selinuxEnforceMode, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.clusterCompatibilityVersion(), clusterCompatibilityVersion, 2).withAutoPlacement());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());

        automaticLargePage.setValue((VdsTransparentHugePagesState) getDetailModel().getAutomaticLargePage());

        ArrayList<ValueLabel<Integer>> physicalMemoryDetailsArray =
                new ArrayList<>(Arrays.<ValueLabel<Integer>>asList(physicalMemory, usedMemory, freeMemory));

        ArrayList<ValueLabel<Long>> swapSizeDetailsArray =
                new ArrayList<>(Arrays.<ValueLabel<Long>>asList(swapTotal, usedSwap, swapFree));

        physicalMemoryDetails.setValue(physicalMemoryDetailsArray);
        swapSizeDetails.setValue(swapSizeDetailsArray);

        maxSchedulingMemory.setValue(selectedItem.getMaxSchedulingMemory());

        formBuilder.update(getDetailModel());
    }

    @Override
    public void clearAlerts() {
        // Remove all the alert widgets and make the panel invisible:
        alertsList.clear();
        alertsPanel.setVisible(false);
    }

    @Override
    public void addAlert(Widget alertWidget) {
        addAlert(alertWidget, AlertType.ALERT);
    }

    @Override
    public void addAlert(Widget alertWidget, AlertType type) {
        // Add the composite panel to the alerts panel:
        alertsList.add(new InLineAlertWidget(alertWidget, type));

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }

}
