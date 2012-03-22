package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.MemorySizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.DetailsLabel;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberLabel;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;
import org.ovirt.engine.ui.webadmin.widget.label.VersionLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabHostGeneralView extends AbstractSubTabFormView<VDS, HostListModel, HostGeneralModel> implements SubTabHostGeneralPresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostGeneralModel, SubTabHostGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    // We need this in order to find the icon for alert messages:
    private final ApplicationResources resources;

    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel kvmVersion = new TextBoxLabel();
    TextBoxLabel spiceVersion = new TextBoxLabel();
    TextBoxLabel kernelVersion = new TextBoxLabel();
    TextBoxLabel iScsiInitiatorName = new TextBoxLabel();
    TextBoxLabel cpuName = new TextBoxLabel();
    TextBoxLabel cpuType = new TextBoxLabel();
    VersionLabel vdsmVersion = new VersionLabel();
    PercentLabel<Integer> sharedMemory = new PercentLabel<Integer>();
    BooleanLabel memoryPageSharing = new BooleanLabel("Active", "Inactive");
    NullableNumberLabel<Integer> activeVms = new NullableNumberLabel<Integer>();
    NullableNumberLabel<Integer> numberOfCPUs = new NullableNumberLabel<Integer>();

    MemorySizeLabel<Integer> physicalMemory;
    MemorySizeLabel<Integer> usedMemory;
    MemorySizeLabel<Integer> freeMemory;

    MemorySizeLabel<Long> swapTotal;
    MemorySizeLabel<Long> usedSwap;
    MemorySizeLabel<Long> swapFree;

    @Ignore
    DetailsLabel<ArrayList<ValueLabel<Integer>>, Integer> physicalMemoryDetails =
            new DetailsLabel<ArrayList<ValueLabel<Integer>>, Integer>("total", "used", "free");

    @Ignore
    DetailsLabel<ArrayList<ValueLabel<Long>>, Long> swapSizeDetails =
            new DetailsLabel<ArrayList<ValueLabel<Long>>, Long>("total", "used", "free");

    @Ignore
    EnumLabel<VdsTransparentHugePagesState> automaticLargePage = new EnumLabel<VdsTransparentHugePagesState>();

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

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostGeneralView(DetailModelProvider<HostListModel, HostGeneralModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        initMemorySizeLabels(constants);

        // Inject a reference to the resources:
        this.resources = resources;

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setColumnsWidth("230px", "120px", "270px");
        formBuilder.addFormItem(new FormItem("OS Version", oS, 0, 0));
        formBuilder.addFormItem(new FormItem("Kernel Version", kernelVersion, 1, 0));
        formBuilder.addFormItem(new FormItem("KVM Version", kvmVersion, 2, 0));
        formBuilder.addFormItem(new FormItem("VDSM Version", vdsmVersion, 3, 0));
        formBuilder.addFormItem(new FormItem("SPICE Version", spiceVersion, 4, 0));
        formBuilder.addFormItem(new FormItem("iSCSI Initiator Name", iScsiInitiatorName, 5, 0));

        formBuilder.addFormItem(new FormItem("Active VMs", activeVms, 0, 1));
        formBuilder.addFormItem(new FormItem("Memory Page Sharing", memoryPageSharing, 1, 1));
        formBuilder.addFormItem(new FormItem("Automatic Large Pages", automaticLargePage, 2, 1));
        formBuilder.addFormItem(new FormItem("Number of CPUs", numberOfCPUs, 3, 1));
        formBuilder.addFormItem(new FormItem("CPU Name", cpuName, 4, 1));
        formBuilder.addFormItem(new FormItem("CPU Type", cpuType, 5, 1));

        formBuilder.addFormItem(new FormItem("Physical Memory", physicalMemoryDetails, 0, 2));
        formBuilder.addFormItem(new FormItem("Swap Size", swapSizeDetails, 1, 2));
        formBuilder.addFormItem(new FormItem("Shared Memory", sharedMemory, 2, 2));
    }

    void initMemorySizeLabels(ApplicationConstants constants) {
        this.physicalMemory = new MemorySizeLabel<Integer>(constants);
        this.usedMemory = new MemorySizeLabel<Integer>(constants);
        this.freeMemory = new MemorySizeLabel<Integer>(constants);

        this.swapTotal = new MemorySizeLabel<Long>(constants);
        this.usedSwap = new MemorySizeLabel<Long>(constants);
        this.swapFree = new MemorySizeLabel<Long>(constants);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        Driver.driver.edit(getDetailModel());

        automaticLargePage.setValue((VdsTransparentHugePagesState) getDetailModel().getAutomaticLargePage());

        ArrayList<ValueLabel<Integer>> physicalMemoryDetailsArray =
                new ArrayList<ValueLabel<Integer>>(Arrays.asList(physicalMemory, usedMemory, freeMemory));

        ArrayList<ValueLabel<Long>> swapSizeDetailsArray =
                new ArrayList<ValueLabel<Long>>(Arrays.asList(swapTotal, usedSwap, swapFree));

        physicalMemoryDetails.setValue(physicalMemoryDetailsArray);
        swapSizeDetails.setValue(swapSizeDetailsArray);

        formBuilder.showForm(getDetailModel());
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
        alertIcon.getElement().getStyle().setProperty("display", "inline");
        alertWidget.getElement().getStyle().setProperty("display", "inline");
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
