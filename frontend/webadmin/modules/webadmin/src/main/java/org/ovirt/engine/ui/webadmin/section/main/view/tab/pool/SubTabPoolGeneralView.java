package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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

    StringValueLabel cpuInfo = new StringValueLabel();
    StringValueLabel graphicsType = new StringValueLabel();
    StringValueLabel defaultDisplayType = new StringValueLabel();
    StringValueLabel defaultHost = new StringValueLabel();
    StringValueLabel definedMemory = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel domain = new StringValueLabel();
    StringValueLabel minAllocatedMemory = new StringValueLabel();
    StringValueLabel name = new StringValueLabel();
    StringValueLabel origin = new StringValueLabel();
    @Ignore
    StringValueLabel oS = new StringValueLabel();
    StringValueLabel template = new StringValueLabel();
    StringValueLabel timeZone = new StringValueLabel();
    StringValueLabel usbPolicy = new StringValueLabel();
    StringValueLabel quotaName = new StringValueLabel();

    @Ignore
    StringValueLabel isStateless = new StringValueLabel();

    @Ignore
    StringValueLabel monitorCount = new StringValueLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = org.ovirt.engine.ui.common.gin.AssetProvider.getTemplates();

    @Inject
    public SubTabPoolGeneralView(DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 7);
        formBuilder.setRelativeColumnWidth(0, 3);
        formBuilder.setRelativeColumnWidth(1, 4);
        formBuilder.setRelativeColumnWidth(2, 5);

        formBuilder.addFormItem(new FormItem(constants.namePoolGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionPoolGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templatePoolGeneral(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osPoolGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.graphicsProtocol(), graphicsType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.quota(), quotaName, 6, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemPoolGeneral(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGaurPoolGeneral(), minAllocatedMemory, 1, 1));

        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(templates.numOfCpuCoresTooltip());
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresPoolGeneral(), cpuInfoWithTooltip, 2, 1));
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
        monitorCount.setValue(String.valueOf(getDetailModel().getMonitorCount()));
        isStateless.setValue(Boolean.toString(getDetailModel().getIsStateless()));
        oS.setValue(AsyncDataProvider.getInstance().getOsName(getDetailModel().getOS()));

        formBuilder.update(getDetailModel());
    }

}
