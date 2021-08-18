package org.ovirt.engine.ui.common.widget.uicommon.template;

import static org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel.ARCHITECTURE;
import static org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel.BIOS_TYPE;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.FormWidgetWithTooltippedIcon;
import org.ovirt.engine.ui.common.widget.dialog.WarnIcon;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.BiosTypeLabel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class TemplateGeneralModelForm extends AbstractModelBoundFormWidget<TemplateGeneralModel> {

    interface Driver extends UiCommonEditorDriver<TemplateGeneralModel, TemplateGeneralModelForm> {
    }

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    StringValueLabel name = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel hostCluster = new StringValueLabel();
    StringValueLabel definedMemory = new StringValueLabel();
    @Path("OS")
    StringValueLabel oS = new StringValueLabel();
    BiosTypeRenderer biosTypeRenderer = new BiosTypeRenderer();
    BiosTypeLabel biosType = new BiosTypeLabel(biosTypeRenderer);
    FormWidgetWithTooltippedIcon biosTypeWithIcon = new FormWidgetWithTooltippedIcon(biosType, WarnIcon.class);
    StringValueLabel cpuInfo = new StringValueLabel();
    StringValueLabel graphicsType = new StringValueLabel();
    StringValueLabel defaultDisplayType = new StringValueLabel();
    StringValueLabel origin = new StringValueLabel();
    StringValueLabel priority = new StringValueLabel();
    StringValueLabel usbPolicy = new StringValueLabel();
    StringValueLabel domain = new StringValueLabel();
    StringValueLabel timeZone = new StringValueLabel();
    StringValueLabel quotaName = new StringValueLabel();
    StringValueLabel templateId = new StringValueLabel();
    StringValueLabel optimizedForSystemProfile = new StringValueLabel();

    BooleanLabel isHighlyAvailable;

    @Ignore
    StringValueLabel monitorCount = new StringValueLabel();
    @Ignore
    StringValueLabel isStateless = new StringValueLabel();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    public TemplateGeneralModelForm(ModelProvider<TemplateGeneralModel> modelProvider) {
        super(modelProvider, 3, 8);
    }

    /**
     * Initialize the form. Call this after ID has been set on the form,
     * so that form fields can use the ID as their prefix.
     */
    public void initialize() {

        driver.initialize(this);
        isHighlyAvailable = new BooleanLabel(constants.yes(), constants.no());

        // Build a form using the FormBuilder
        formBuilder.addFormItem(new FormItem(constants.nameTemplateGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionTemplateGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.hostClusterTemplateGeneral(), hostCluster, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osTemplateGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.biosTypeGeneral(), biosTypeWithIcon, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.graphicsProtocol(), graphicsType, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.videoType(), defaultDisplayType, 6, 0));
        formBuilder.addFormItem(new FormItem(constants.optimizedFor(), optimizedForSystemProfile, 7, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemTemplateGeneral(), definedMemory, 0, 1));

        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(SafeHtmlUtils.fromString(constants.numOfCpuCoresTooltip()));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresTemplateGeneral(), cpuInfoWithTooltip, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsTemplateGeneral(), monitorCount, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.highlyAvailTemplateGeneral(), isHighlyAvailable, 3, 1));
        formBuilder.addFormItem(new FormItem(constants.priorityTemplateGeneral(), priority, 4, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyTemplateGeneral(), usbPolicy, 5, 1) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem(constants.originTemplateGeneral(), origin, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.isStatelessTemplateGeneral(), isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.templateId(), templateId, 2, 2));
        formBuilder.addFormItem(new FormItem(constants.domainTemplateGeneral(), domain, 3, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.tzTemplateGeneral(), timeZone, 4, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getHasTimeZone();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.quotaTemplateGeneral(), quotaName, 5, 2) {
            @Override
            public boolean getIsAvailable() {
                return getModel().isQuotaAvailable();
            }
        }.withDefaultValue(constants.notConfigured(), () -> {
            String quotaName = getModel().getQuotaName();
            return quotaName == null || "".equals(quotaName);
        }));
    }

    @Override
    protected void doEdit(TemplateGeneralModel model) {
        driver.edit(model);

        // Required because of type conversion
        monitorCount.setValue(Integer.toString(getModel().getMonitorCount()));
        isStateless.setValue(Boolean.toString(getModel().getIsStateless()));

        updateBiosTypeWidget(biosTypeWithIcon);

        getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(BIOS_TYPE)) {
                    updateBiosTypeWidget(biosTypeWithIcon);
                }
            }
        });

        getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(ARCHITECTURE)) {
                    updateBiosTypeWidget(biosTypeWithIcon);
                    // change of the architecture changes the bios type rendering so we need to trigger the redraw
                    getModel().onPropertyChanged(EntityModel.ENTITY);
                }
            }
        });
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void updateBiosTypeWidget(FormWidgetWithTooltippedIcon widgetWithIcon) {
        if (getModel() == null || getModel().getEntity() == null) {
            widgetWithIcon.setIconVisible(false);
            return;
        }

        biosTypeRenderer.setArchitectureType(getModel().getArchitecture());
        widgetWithIcon.setIconVisible(
                getModel().getEntity().getBiosType() != getModel().getEntity().getClusterBiosType());
        widgetWithIcon.setIconTooltipText(messages.biosTypeWarning(
                biosTypeRenderer.render(getModel().getEntity().getClusterBiosType())));
    }
}
