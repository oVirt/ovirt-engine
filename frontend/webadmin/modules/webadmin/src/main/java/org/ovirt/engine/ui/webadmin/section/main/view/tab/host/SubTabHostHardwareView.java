package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;


import java.util.EnumMap;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHardwarePresenter;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberTextBoxLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabHostHardwareView extends AbstractSubTabFormView<VDS, HostListModel, HostHardwareGeneralModel> implements SubTabHostHardwarePresenter.ViewDef, Editor<HostHardwareGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostHardwareGeneralModel, SubTabHostHardwareView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostHardwareView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    TextBoxLabel hardwareManufacturer = new TextBoxLabel();
    TextBoxLabel hardwareProductName = new TextBoxLabel();
    TextBoxLabel hardwareSerialNumber = new TextBoxLabel();
    TextBoxLabel hardwareVersion = new TextBoxLabel();
    TextBoxLabel hardwareUUID = new TextBoxLabel();
    TextBoxLabel hardwareFamily = new TextBoxLabel();
    TextBoxLabel cpuType = new TextBoxLabel();
    TextBoxLabel cpuModel = new TextBoxLabel();
    NullableNumberTextBoxLabel<Integer> numberOfSockets = new NullableNumberTextBoxLabel<Integer>(constants.unknown());
    NullableNumberTextBoxLabel<Integer> coresPerSocket = new NullableNumberTextBoxLabel<Integer>(constants.unknown());
    TextBoxLabel threadsPerCore = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    @UiField
    FlowPanel hbaInventory;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostHardwareView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostHardwareView(DetailModelProvider<HostListModel, HostHardwareGeneralModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 4);
        formBuilder.addFormItem(new FormItem(constants.hardwareManufacturerGeneral(), hardwareManufacturer, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareFamilyGeneral(), hardwareFamily, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.hardwareProductNameGeneral(), hardwareProductName, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.hardwareVersionGeneral(), hardwareVersion, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareUUIDGeneral(), hardwareUUID, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.hardwareSerialNumberGeneral(), hardwareSerialNumber, 1, 2));

        formBuilder.addFormItem(new FormItem(constants.cpuModelHostGeneral(), cpuModel, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.cpuTypeHostGeneral(), cpuType, 1).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.numOfSocketsHostGeneral(), numberOfSockets, 2).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.numOfCoresPerSocketHostGeneral(), coresPerSocket, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.numOfThreadsPerCoreHostGeneral(), threadsPerCore, 1).withAutoPlacement());

    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());

        refreshHBADeviceInfo(selectedItem);

        formBuilder.update(getDetailModel());
    }

    private void refreshHBADeviceInfo(VDS selectedItem) {
        /* refresh all the information about HBA (FC, iSCSI) devices */
        hbaInventory.clear();

        if (selectedItem != null && getDetailModel().getHbaDevices() != null) {

            /*
             * traverse the model and get all the HBAs
             */
            for (EnumMap<HostHardwareGeneralModel.HbaDeviceKeys, String> hbaDevice : getDetailModel().getHbaDevices()) {
                GeneralFormPanel hbaFormPanel = new GeneralFormPanel() {
                    {
                        getElement().getStyle().setFloat(Float.LEFT);
                        getElement().getStyle().setBorderWidth(1, Unit.PX);
                        getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
                        getElement().getStyle().setBorderColor("black"); //$NON-NLS-1$
                        getElement().getStyle().setProperty("width", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                };

                TextBoxLabel interfaceName = new TextBoxLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.MODEL_NAME));
                TextBoxLabel interfaceType = new TextBoxLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.TYPE));
                TextBoxLabel interfaceWWNN = new TextBoxLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNN));
                TextBoxLabel portWWPNs = new TextBoxLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNPS));

                FormBuilder hbaFormBuilder = new FormBuilder(hbaFormPanel, 1, 4);
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaModelName(), interfaceName, 0, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaDeviceType(), interfaceType, 1, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWNN(), interfaceWWNN, 2, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWPNs(), portWWPNs, 3, 0));

                hbaInventory.add(hbaFormPanel);
            }
        }
    }

}
