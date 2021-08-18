package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import static org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel.ARCHITECTURE;
import static org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel.BIOS_TYPE;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.FormWidgetWithTooltippedIcon;
import org.ovirt.engine.ui.common.widget.dialog.WarnIcon;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BiosTypeLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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

    interface Driver extends UiCommonEditorDriver<PoolGeneralModel, SubTabPoolGeneralView> {
    }

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

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
    BiosTypeRenderer biosTypeRenderer = new BiosTypeRenderer();
    BiosTypeLabel biosType = new BiosTypeLabel(biosTypeRenderer);
    FormWidgetWithTooltippedIcon biosTypeWithIcon = new FormWidgetWithTooltippedIcon(biosType, WarnIcon.class);
    StringValueLabel template = new StringValueLabel();
    StringValueLabel timeZone = new StringValueLabel();
    StringValueLabel usbPolicy = new StringValueLabel();
    StringValueLabel quotaName = new StringValueLabel();
    StringValueLabel optimizedForSystemProfile = new StringValueLabel();

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

    @Inject
    public SubTabPoolGeneralView(DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 9);

        formBuilder.addFormItem(new FormItem(constants.namePoolGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionPoolGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templatePoolGeneral(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osPoolGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.biosTypeGeneral(), biosTypeWithIcon, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.graphicsProtocol(), graphicsType, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 6, 0));
        formBuilder.addFormItem(new FormItem(constants.quota(), quotaName, 7, 0));
        formBuilder.addFormItem(new FormItem(constants.optimizedFor(), optimizedForSystemProfile, 8, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemPoolGeneral(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGaurPoolGeneral(), minAllocatedMemory, 1, 1));

        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(SafeHtmlUtils.fromString(constants.numOfCpuCoresTooltip()));
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
    public void setMainSelectedItem(VmPool selectedItem) {
        driver.edit(getDetailModel());

        // Required because of type conversion
        monitorCount.setValue(String.valueOf(getDetailModel().getMonitorCount()));
        isStateless.setValue(Boolean.toString(getDetailModel().getIsStateless()));
        oS.setValue(AsyncDataProvider.getInstance().getOsName(getDetailModel().getOS()));

        formBuilder.update(getDetailModel());

        updateBiosTypeWidget(biosTypeWithIcon);

        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(BIOS_TYPE)) {
                    updateBiosTypeWidget(biosTypeWithIcon);
                }
            }
        });

        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(ARCHITECTURE)) {
                    updateBiosTypeWidget(biosTypeWithIcon);
                    // change of the architecture changes the bios type rendering so we need to trigger the redraw
                    getDetailModel().onPropertyChanged(EntityModel.ENTITY);
                }
            }
        });
    }

    private void updateBiosTypeWidget(FormWidgetWithTooltippedIcon widgetWithIcon) {
        if (getDetailModel() == null || getDetailModel().getvm() == null) {
            widgetWithIcon.setIconVisible(false);
            return;
        }
        biosTypeRenderer.setArchitectureType(getDetailModel().getArchitecture());
        widgetWithIcon.setIconVisible(
                getDetailModel().getvm().getBiosType() != getDetailModel().getvm().getClusterBiosType());
        widgetWithIcon.setIconTooltipText(messages.biosTypeWarning(
                biosTypeRenderer.render(getDetailModel().getvm().getClusterBiosType())));
    }
}
