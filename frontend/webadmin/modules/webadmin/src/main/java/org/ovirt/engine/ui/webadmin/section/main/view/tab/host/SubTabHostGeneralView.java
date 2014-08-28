package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.EnumTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.MemorySizeTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget;
import org.ovirt.engine.ui.webadmin.widget.label.DetailsTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.PercentTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.VersionTextBoxLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabHostGeneralView extends AbstractSubTabFormView<VDS, HostListModel, HostGeneralModel> implements SubTabHostGeneralPresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostGeneralModel, SubTabHostGeneralView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    // We need this in order to find the icon for alert messages:
    private final ApplicationResources resources;
    private final ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    @Path("OS")
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel kvmVersion = new TextBoxLabel();
    VersionTextBoxLabel libvirtVersion = new VersionTextBoxLabel();
    TextBoxLabel spiceVersion = new TextBoxLabel();
    TextBoxLabel kernelVersion = new TextBoxLabel();
    VersionTextBoxLabel glusterVersion = new VersionTextBoxLabel();
    @Path("IScsiInitiatorName")
    TextBoxLabel iScsiInitiatorName = new TextBoxLabel();
    VersionTextBoxLabel vdsmVersion = new VersionTextBoxLabel();
    PercentTextBoxLabel<Integer> sharedMemory = new PercentTextBoxLabel<Integer>();
    BooleanTextBoxLabel memoryPageSharing = new BooleanTextBoxLabel(constants.active(), constants.inactive());
    NullableNumberTextBoxLabel<Integer> activeVms = new NullableNumberTextBoxLabel<Integer>();
    NullableNumberTextBoxLabel<Integer> logicalCores = new NullableNumberTextBoxLabel<Integer>();
    TextBoxLabel spmPriority = new TextBoxLabel();
    TextBoxLabel hostedEngineHa = new TextBoxLabel();
    FullDateTimeLabel bootTime = new FullDateTimeLabel();
    TextBoxLabel kdumpStatus = new TextBoxLabel();
    TextBoxLabel selinuxEnforceMode = new TextBoxLabel();

    MemorySizeTextBoxLabel<Integer> physicalMemory;
    MemorySizeTextBoxLabel<Integer> usedMemory;
    MemorySizeTextBoxLabel<Integer> freeMemory;

    MemorySizeTextBoxLabel<Long> swapTotal;
    MemorySizeTextBoxLabel<Long> usedSwap;
    MemorySizeTextBoxLabel<Long> swapFree;
    MemorySizeTextBoxLabel<Float> maxSchedulingMemory;

    BooleanTextBoxLabel liveSnapshotSupport = new BooleanTextBoxLabel(constants.active(), constants.inactive());

    @Ignore
    DetailsTextBoxLabel<ArrayList<TextBoxLabelBase<Integer>>, Integer> physicalMemoryDetails =
            new DetailsTextBoxLabel<ArrayList<TextBoxLabelBase<Integer>>, Integer>(constants.total(),
                    constants.used(),
                    constants.free());

    @Ignore
    DetailsTextBoxLabel<ArrayList<TextBoxLabelBase<Long>>, Long> swapSizeDetails =
            new DetailsTextBoxLabel<ArrayList<TextBoxLabelBase<Long>>, Long>(constants.total(),
                    constants.used(),
                    constants.free());

    @Ignore
    EnumTextBoxLabel<VdsTransparentHugePagesState> automaticLargePage = new EnumTextBoxLabel<VdsTransparentHugePagesState>();

    @UiField(provided = true)
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

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostGeneralView(DetailModelProvider<HostListModel, HostGeneralModel> modelProvider,
            ApplicationResources resources) {
        super(modelProvider);

        // Inject a reference to the resources:
        this.resources = resources;
        initMemorySizeLabels();

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
        boolean glusterSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 9);

        formBuilder.addFormItem(new FormItem(constants.osVersionHostGeneral(), oS, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kernelVersionHostGeneral(), kernelVersion, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kvmVersionHostGeneral(), kvmVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.libvirtVersionHostGeneral(), libvirtVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.vdsmVersionHostGeneral(), vdsmVersion, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.spiceVersionHostGeneral(), spiceVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.glusterVersionHostGeneral(), glusterVersion, 0, glusterSupported).withAutoPlacement());

        formBuilder.addFormItem(new FormItem(constants.spmPriority(), spmPriority, 0, 1, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.activeVmsHostGeneral(), activeVms, 1, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.logicalCores(), logicalCores, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.bootTimeHostGeneral(), bootTime, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.hostedEngineHaHostGeneral(), hostedEngineHa, 1, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.isciInitNameHostGeneral(), iScsiInitiatorName, 1, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kdumpStatus(), kdumpStatus, 1).withAutoPlacement());

        formBuilder.addFormItem(new FormItem(constants.physMemHostGeneral(), physicalMemoryDetails, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.swapSizeHostGeneral(), swapSizeDetails, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.sharedMemHostGeneral(), sharedMemory, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.maxSchedulingMemory(), maxSchedulingMemory, 2, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.memPageSharingHostGeneral(), memoryPageSharing, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.autoLargePagesHostGeneral(), automaticLargePage, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.selinuxModeGeneral(), selinuxEnforceMode, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.liveSnapshotSupportHostGeneral(), liveSnapshotSupport, 2).withAutoPlacement());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initMemorySizeLabels() {
        this.physicalMemory = new MemorySizeTextBoxLabel<Integer>(constants);
        this.usedMemory = new MemorySizeTextBoxLabel<Integer>(constants);
        this.freeMemory = new MemorySizeTextBoxLabel<Integer>(constants);

        this.swapTotal = new MemorySizeTextBoxLabel<Long>(constants);
        this.usedSwap = new MemorySizeTextBoxLabel<Long>(constants);
        this.swapFree = new MemorySizeTextBoxLabel<Long>(constants);

        this.maxSchedulingMemory = new MemorySizeTextBoxLabel<Float>(constants);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());

        automaticLargePage.setValue((VdsTransparentHugePagesState) getDetailModel().getAutomaticLargePage());

        ArrayList<TextBoxLabelBase<Integer>> physicalMemoryDetailsArray =
                new ArrayList<TextBoxLabelBase<Integer>>(Arrays.asList(physicalMemory, usedMemory, freeMemory));

        ArrayList<TextBoxLabelBase<Long>> swapSizeDetailsArray =
                new ArrayList<TextBoxLabelBase<Long>>(Arrays.asList(swapTotal, usedSwap, swapFree));

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
        // Add the composite panel to the alerts panel:
        alertsList.add(new InLineAlertWidget(resources, alertWidget));

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }

}
