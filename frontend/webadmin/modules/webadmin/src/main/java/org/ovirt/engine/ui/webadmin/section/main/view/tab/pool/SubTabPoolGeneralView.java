package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.form.GeneralFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabPoolGeneralView extends AbstractSubTabFormView<vm_pools, PoolListModel, PoolGeneralModel> implements SubTabPoolGeneralPresenter.ViewDef, Editor<PoolGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabPoolGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<PoolGeneralModel, SubTabPoolGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel defaultHost = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel domain = new TextBoxLabel();
    TextBoxLabel minAllocatedMemory = new TextBoxLabel();
    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel origin = new TextBoxLabel();
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel storageDomain = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel timeZone = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();

    @Ignore
    // TODO: primitive field
    TextBoxLabel isStateless = new TextBoxLabel();

    @Ignore
    // TODO: primitive field
    TextBoxLabel monitorCount = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabPoolGeneralView(DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Template", template, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));

        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Physical Memory Guaranteed", minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 3, 1));
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 4, 1));
        formBuilder.addFormItem(new FormItem("Resides on Storage Domain", storageDomain, 5, 1, "HasStorageDomain") {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasStorageDomain();
            }
        });

        formBuilder.addFormItem(new FormItem("Origin", origin, 0, 2));
        formBuilder.addFormItem(new FormItem("Is Stateless", isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem("Run On", defaultHost, 2, 2));
        formBuilder.addFormItem(new FormItem("Domain", domain, 3, 2, "HasDomain") {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem("Time Zone", timeZone, 4, 2, "HasTimeZone") {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasTimeZone();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(vm_pools selectedItem) {
        Driver.driver.edit(getDetailModel());

        // TODO required because of GWT#5864
        monitorCount.setText(String.valueOf(getDetailModel().getMonitorCount()));
        isStateless.setText(Boolean.toString(getDetailModel().getIsStateless()));

        formBuilder.showForm(getDetailModel(), Driver.driver);
    }

}
