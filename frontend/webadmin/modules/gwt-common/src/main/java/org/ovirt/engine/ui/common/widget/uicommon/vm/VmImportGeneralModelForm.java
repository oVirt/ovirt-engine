package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBox;
import org.ovirt.engine.ui.common.widget.editor.TextBoxChanger;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.FormItem.DefaultValueCondition;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportGeneralModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class VmImportGeneralModelForm extends AbstractModelBoundFormWidget<VmImportGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<VmImportGeneralModel, VmImportGeneralModelForm> {
    }

    @Path("name.entity")
    TextBoxChanger name = new TextBoxChanger();
    TextBoxLabel description = new TextBoxLabel();
    @UiField(provided = true)
    @Path("operatingSystems.selectedItem")
    ListModelListBox<Integer> operatingSystems;
    @Path("OS")
    TextBoxLabel os = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel priority = new TextBoxLabel();
    TextBoxLabel minAllocatedMemory = new TextBoxLabel();
    TextBoxLabel guestFreeCachedBufferedMemInfo = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel defaultHost = new TextBoxLabel();
    TextBoxLabel customProperties = new TextBoxLabel();
    TextBoxLabel domain = new TextBoxLabel();
    TextBoxLabel compatibilityVersion = new TextBoxLabel();
    TextBoxLabel vmId = new TextBoxLabel();
    TextBoxLabel fqdn = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel guestCpuCount = new TextBoxLabel();
    TextBoxLabel quotaName = new TextBoxLabel();
    TextBoxLabel origin = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    private final static CommonApplicationConstants constants = AssetProvider.getConstants();
    private final Driver driver = GWT.create(Driver.class);

    public VmImportGeneralModelForm(ModelProvider<VmImportGeneralModel> modelProvider) {
        super(modelProvider, 3, 7);

        operatingSystems = new ListModelListBox<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                return AsyncDataProvider.getInstance().getOsName(object);
            }
        });
    }

    @Override
    protected void doEdit(VmImportGeneralModel model) {
        driver.edit(model);

        // Required because of type conversion
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
    }

    private Widget getOperatingSystemWidget() {
        ImportSource source = getModel().getSource();
        return source == ImportSource.EXPORT_DOMAIN ? os : operatingSystems;
    }

    public void initialize() {
        driver.initialize(this);

        name.setWidth("130px"); //$NON-NLS-1$
        operatingSystems.setWidth("130px"); //$NON-NLS-1$

        formBuilder.addFormItem(new FormItem(constants.nameVm(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.osVm(), getOperatingSystemWidget(), 1, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionVm(), description, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.templateVm(), template, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.priorityVm(), priority, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.definedMemoryVm(), definedMemory, 6, 0));

        formBuilder.addFormItem(new FormItem(constants.physMemGauranteedVm(), minAllocatedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.guestFreeCachedBufferedMemInfo(), guestFreeCachedBufferedMemInfo, 1, 1)
        .withDefaultValue(constants.notConfigured(), new DefaultValueCondition() {
            @Override
            public boolean showDefaultValue() {
                return getModel().getGuestFreeCachedBufferedMemInfo() == null;
            }
        }));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresVm(), cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.GuestCpuCount(), guestCpuCount, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsVm(), monitorCount, 4, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyVm(), usbPolicy, 5, 1));
        formBuilder.addFormItem(new FormItem(constants.originVm(), origin, 6, 1));

        formBuilder.addFormItem(new FormItem(constants.runOnVm(), defaultHost, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.customPropertiesVm(), customProperties, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.clusterCompatibilityVersionVm(), compatibilityVersion, 2, 2));
        formBuilder.addFormItem(new FormItem(constants.vmId(), vmId, 3, 2));

        formBuilder.addFormItem(new FormItem(constants.quotaVm(), quotaName, 4, 2) {
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
        formBuilder.addFormItem(new FormItem(constants.domainVm(), domain, 5, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasDomain();
            }
        });

        formBuilder.addFormItem(new FormItem(constants.fqdn(), fqdn, 6, 2) {
            @Override
            public boolean getIsAvailable() {
                String fqdn = getModel().getFqdn();
                return !(fqdn == null || fqdn.isEmpty());
            }
        });
    }
}
