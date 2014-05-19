package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class PoolGeneralModelForm extends AbstractModelBoundFormWidget<PoolGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<PoolGeneralModel, PoolGeneralModelForm> {
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel minAllocatedMemory = new TextBoxLabel();
    @Ignore
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel graphicsType = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel defaultHost = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    private final CommonApplicationConstants constants;

    private final Driver driver = GWT.create(Driver.class);

    public PoolGeneralModelForm(ModelProvider<PoolGeneralModel> modelProvider, CommonApplicationConstants constants) {
        super(modelProvider, 3, 6);
        this.constants = constants;
    }

    /**
     * Initialize the form. Call this after ID has been set on the form,
     * so that form fields can use the ID as their prefix.
     */
    public void initialize() {

        driver.initialize(this);

        formBuilder.addFormItem(new FormItem(constants.namePoolGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionPoolGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templatePoolGeneral(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osPoolGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.graphicsProtocol(), graphicsType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 5, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemPoolGeneral(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGaurPoolGeneral(), minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresPoolGeneral(), cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsPoolGeneral(), monitorCount, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyPoolGeneral(), usbPolicy, 4, 1));

        formBuilder.addFormItem(new FormItem(constants.runOnPoolGeneral(), defaultHost, 0, 2));
    }

    @Override
    protected void doEdit(PoolGeneralModel model) {
        driver.edit(model);

        // Required because of type conversion
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
        oS.setText(AsyncDataProvider.getInstance().getOsName(getModel().getOS()));
    }

}
