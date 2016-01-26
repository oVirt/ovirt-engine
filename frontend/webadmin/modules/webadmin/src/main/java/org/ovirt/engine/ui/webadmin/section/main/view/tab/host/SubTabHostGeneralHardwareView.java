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
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralHardwarePresenter;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberValueLabel;
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

public class SubTabHostGeneralHardwareView
    extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostHardwareGeneralModel>
    implements SubTabHostGeneralHardwarePresenter.ViewDef, Editor<HostHardwareGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostHardwareGeneralModel, SubTabHostGeneralHardwareView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGeneralHardwareView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    StringValueLabel hardwareManufacturer = new StringValueLabel();
    StringValueLabel hardwareProductName = new StringValueLabel();
    StringValueLabel hardwareSerialNumber = new StringValueLabel();
    StringValueLabel hardwareVersion = new StringValueLabel();
    StringValueLabel hardwareUUID = new StringValueLabel();
    StringValueLabel hardwareFamily = new StringValueLabel();
    StringValueLabel cpuType = new StringValueLabel();
    StringValueLabel cpuModel = new StringValueLabel();
    NullableNumberValueLabel<Integer> numberOfSockets = new NullableNumberValueLabel<>(constants.unknown());
    StringValueLabel coresPerSocket = new StringValueLabel();
    StringValueLabel threadsPerCore = new StringValueLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    @UiField
    FlowPanel hbaInventory;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralHardwareView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostGeneralHardwareView(DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> modelProvider) {
        super(modelProvider);

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 4);
        formBuilder.setRelativeColumnWidth(0, 4);
        formBuilder.setRelativeColumnWidth(1, 5);
        formBuilder.setRelativeColumnWidth(2, 3);
        formBuilder.addFormItem(new FormItem(constants.hardwareManufacturerGeneral(), hardwareManufacturer, 0, 0), 5, 7);
        formBuilder.addFormItem(new FormItem(constants.hardwareVersionGeneral(), hardwareVersion, 1, 0), 5, 7);
        formBuilder.addFormItem(new FormItem(constants.cpuModelHostGeneral(), cpuModel, 2, 0), 5, 7);
        formBuilder.addFormItem(new FormItem(constants.numOfCoresPerSocketHostGeneral(), coresPerSocket, 3, 0), 5, 7);

        formBuilder.addFormItem(new FormItem(constants.hardwareFamilyGeneral(), hardwareFamily, 0, 1), 4, 8);
        formBuilder.addFormItem(new FormItem(constants.hardwareUUIDGeneral(), hardwareUUID, 1, 1), 4, 8);
        formBuilder.addFormItem(new FormItem(constants.cpuTypeHostGeneral(), cpuType, 2, 1), 4, 8);
        formBuilder.addFormItem(new FormItem(constants.numOfThreadsPerCoreHostGeneral(), threadsPerCore, 3, 1), 4, 8);

        formBuilder.addFormItem(new FormItem(constants.hardwareProductNameGeneral(), hardwareProductName, 0, 2), 4, 8);
        formBuilder.addFormItem(new FormItem(constants.hardwareSerialNumberGeneral(), hardwareSerialNumber, 1, 2), 4, 8);
        formBuilder.addFormItem(new FormItem(constants.numOfSocketsHostGeneral(), numberOfSockets, 2, 2), 4, 8);

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
        /* refresh all the information about Host Bus Adapter (FC, iSCSI) devices */
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
                        getElement().getStyle().setMarginLeft(5, Unit.PX);
                        getElement().getStyle().setMarginBottom(5, Unit.PX);
                        getElement().getStyle().setProperty("width", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                };

                StringValueLabel interfaceName = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.MODEL_NAME));
                StringValueLabel interfaceType = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.TYPE));
                StringValueLabel interfaceWWNN = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNN));
                StringValueLabel portWWPNs = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNPS));

                FormBuilder hbaFormBuilder = new FormBuilder(hbaFormPanel, 1, 4);
                hbaFormBuilder.setRelativeColumnWidth(0, 12);
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaModelName(), interfaceName, 0, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaDeviceType(), interfaceType, 1, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWNN(), interfaceWWNN, 2, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWPNs(), portWWPNs, 3, 0));
                hbaInventory.add(hbaFormPanel);
            }
        }
    }

}
