package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class PoolGeneralModelForm extends AbstractModelBoundFormWidget<PoolGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<PoolGeneralModel, PoolGeneralModelForm> {
    }

    StringValueLabel name = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel template = new StringValueLabel();
    StringValueLabel definedMemory = new StringValueLabel();
    StringValueLabel minAllocatedMemory = new StringValueLabel();
    @Ignore
    StringValueLabel oS = new StringValueLabel();
    StringValueLabel cpuInfo = new StringValueLabel();
    StringValueLabel graphicsType = new StringValueLabel();
    StringValueLabel defaultDisplayType = new StringValueLabel();
    StringValueLabel usbPolicy = new StringValueLabel();
    StringValueLabel defaultHost = new StringValueLabel();

    @Ignore
    StringValueLabel monitorCount = new StringValueLabel();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private final Driver driver = GWT.create(Driver.class);

    public PoolGeneralModelForm(ModelProvider<PoolGeneralModel> modelProvider) {
        super(modelProvider, 3, 6);
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

        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(templates.numOfCpuCoresTooltip());
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresPoolGeneral(), cpuInfoWithTooltip, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsPoolGeneral(), monitorCount, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyPoolGeneral(), usbPolicy, 4, 1));

        formBuilder.addFormItem(new FormItem(constants.runOnPoolGeneral(), defaultHost, 0, 2));
    }

    @Override
    protected void doEdit(PoolGeneralModel model) {
        driver.edit(model);

        // Required because of type conversion
        monitorCount.setValue(Integer.toString(getModel().getMonitorCount()));
        oS.setValue(AsyncDataProvider.getInstance().getOsName(getModel().getOS()));
    }

}
