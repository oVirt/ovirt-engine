package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.FormItem.DefaultValueCondition;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class VmGeneralModelForm extends AbstractModelBoundFormWidget<VmGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<VmGeneralModel, VmGeneralModelForm> {
    }

    StringValueLabel name = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel quotaName = new StringValueLabel();
    StringValueLabel template = new StringValueLabel();
    StringValueLabel definedMemory = new StringValueLabel();
    StringValueLabel minAllocatedMemory = new StringValueLabel();
    @Path("OS")
    StringValueLabel oS = new StringValueLabel();
    StringValueLabel cpuInfo = new StringValueLabel();
    StringValueLabel guestCpuCount = new StringValueLabel();
    StringValueLabel graphicsType = new StringValueLabel();
    StringValueLabel defaultDisplayType = new StringValueLabel();
    StringValueLabel origin = new StringValueLabel();
    StringValueLabel priority = new StringValueLabel();
    StringValueLabel usbPolicy = new StringValueLabel();
    StringValueLabel defaultHost = new StringValueLabel();
    StringValueLabel customProperties = new StringValueLabel();
    StringValueLabel domain = new StringValueLabel();
    StringValueLabel compatibilityVersion = new StringValueLabel();
    StringValueLabel vmId = new StringValueLabel();
    StringValueLabel fqdn = new StringValueLabel();
    StringValueLabel guestFreeCachedBufferedMemInfo = new StringValueLabel();
    StringValueLabel timeZone = new StringValueLabel();

    BooleanLabel isHighlyAvailable;

    @Ignore
    StringValueLabel monitorCount = new StringValueLabel();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private final Driver driver = GWT.create(Driver.class);

    public VmGeneralModelForm(ModelProvider<VmGeneralModel> modelProvider) {
        super(modelProvider, 3, 9);
    }

    /**
     * Initialize the form. Call this after ID has been set on the form,
     * so that form fields can use the ID as their prefix.
     */
    public void initialize() {

        isHighlyAvailable = new BooleanLabel(constants.yes(), constants.no());

        driver.initialize(this);

        formBuilder.addFormItem(new FormItem(constants.nameVm(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionVm(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templateVm(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osVm(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.graphicsProtocol(), graphicsType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.priorityVm(), priority, 6, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemoryVm(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGauranteedVm(), minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.guestFreeCachedBufferedMemInfo(), guestFreeCachedBufferedMemInfo, 2, 1)
            .withDefaultValue(constants.notConfigured(), new DefaultValueCondition() {
                @Override
                public boolean showDefaultValue() {
                    return getModel().getGuestFreeCachedBufferedMemInfo() == null;
                }
            }));
        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(templates.numOfCpuCoresTooltip());
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresVm(), cpuInfoWithTooltip, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.GuestCpuCount(), guestCpuCount, 4, 1));
        formBuilder.addFormItem(new FormItem(constants.highlyAvailableVm(), isHighlyAvailable, 5, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsVm(), monitorCount, 6, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyVm(), usbPolicy, 7, 1));

        formBuilder.addFormItem(new FormItem(constants.originVm(), origin, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.runOnVm(), defaultHost, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.customPropertiesVm(), customProperties, 2, 2));
        formBuilder.addFormItem(new FormItem(constants.clusterCompatibilityVersionVm(), compatibilityVersion, 3, 2));
        formBuilder.addFormItem(new FormItem(constants.vmId(), vmId, 4, 2));

        formBuilder.addFormItem(new FormItem(constants.quotaVm(), quotaName, 5, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().isQuotaAvailable();
            }
        }.withDefaultValue(constants.notConfigured(), new DefaultValueCondition() {
            @Override
            public boolean showDefaultValue() {
                String quotaName = getModel().getQuotaName();
                return quotaName == null || "".equals(quotaName);
            }
        }));
        formBuilder.addFormItem(new FormItem(constants.domainVm(), domain, 6, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasDomain();
            }
        });

        formBuilder.addFormItem(new FormItem(constants.fqdn(), fqdn, 7, 2) {
            @Override
            public boolean getIsAvailable() {
                String fqdn = getModel().getFqdn();
                return !(fqdn == null || fqdn.isEmpty());
            }
        });
        formBuilder.addFormItem(new FormItem(constants.timeZoneVm(), timeZone, 8, 2 ) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasTimeZone();
            }
        });
    }

    @Override
    protected void doEdit(VmGeneralModel model) {
        driver.edit(model);

        // Required because of type conversion
        monitorCount.setValue(Integer.toString(getModel().getMonitorCount()));
    }

}
