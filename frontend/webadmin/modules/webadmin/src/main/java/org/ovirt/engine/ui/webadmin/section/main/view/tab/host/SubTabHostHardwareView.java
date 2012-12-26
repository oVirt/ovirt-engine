package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabHostHardwareView extends AbstractSubTabFormView<VDS, HostListModel, HostHardwareGeneralModel> implements SubTabHostHardwarePresenter.ViewDef, Editor<HostHardwareGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostHardwareGeneralModel, SubTabHostHardwareView> {
        Driver driver = GWT.create(Driver.class);
    }

    // We need this in order to find the icon for alert messages:
    private final ApplicationResources resources;

    TextBoxLabel hardwareManufacturer = new TextBoxLabel();
    TextBoxLabel hardwareProductName = new TextBoxLabel();
    TextBoxLabel hardwareSerialNumber = new TextBoxLabel();
    TextBoxLabel hardwareVersion = new TextBoxLabel();
    TextBoxLabel hardwareUUID = new TextBoxLabel();
    TextBoxLabel hardwareFamily = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostHardwareView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public SubTabHostHardwareView(DetailModelProvider<HostListModel, HostHardwareGeneralModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);

        // Inject a reference to the resources:
        this.resources = resources;

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setColumnsWidth("230px", "120px", "270px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        formBuilder.addFormItem(new FormItem(constants.hardwareManufacturerGeneral(), hardwareManufacturer, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareProductNameGeneral(), hardwareProductName, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareUUIDGeneral(), hardwareUUID, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareVersionGeneral(), hardwareVersion, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareSerialNumberGeneral(), hardwareSerialNumber, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.hardwareFamilyGeneral(), hardwareFamily, 5, 0));
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        Driver.driver.edit(getDetailModel());

        formBuilder.showForm(getDetailModel());
    }
}
