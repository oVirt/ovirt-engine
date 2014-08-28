package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabPoolGeneralView extends AbstractSubTabFormView<VmPool, PoolListModel, PoolGeneralModel> implements SubTabPoolGeneralPresenter.ViewDef, Editor<PoolGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabPoolGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabPoolGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends SimpleBeanEditorDriver<PoolGeneralModel, SubTabPoolGeneralView> {
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
    @Ignore
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel timeZone = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel quotaName = new TextBoxLabel();

    @Ignore
    TextBoxLabel isStateless = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SubTabPoolGeneralView(DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);

        formBuilder.addFormItem(new FormItem(constants.namePoolGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionPoolGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templatePoolGeneral(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osPoolGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.defaultDisplayTypePoolGeneral(), defaultDisplayType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.quota(), quotaName, 5, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemPoolGeneral(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGaurPoolGeneral(), minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresPoolGeneral(), cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsPoolGeneral(), monitorCount, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyPoolGeneral(), usbPolicy, 4, 1));

        formBuilder.addFormItem(new FormItem(constants.originPoolGeneral(), origin, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.isStatelessPoolGeneral(), isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.runOnPoolGeneral(), defaultHost, 2, 2));
        formBuilder.addFormItem(new FormItem(constants.domainPoolGeneral(), domain, 3, 2, "HasDomain") { //$NON-NLS-1$
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.tzPoolGeneral(), timeZone, 4, 2, "HasTimeZone") { //$NON-NLS-1$
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getHasTimeZone();
            }
        });
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(VmPool selectedItem) {
        driver.edit(getDetailModel());

        // Required because of type conversion
        monitorCount.setText(String.valueOf(getDetailModel().getMonitorCount()));
        isStateless.setText(Boolean.toString(getDetailModel().getIsStateless()));
        oS.setText(AsyncDataProvider.getInstance().getOsName(getDetailModel().getOS()));

        formBuilder.update(getDetailModel());
    }

}
