package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;


import static org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel.ARCHITECTURE_PROPERTY_CHANGE;
import static org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel.CPU_VERB_PROPERTY_CHANGE;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.WidgetWithInfo;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.FormItem.DefaultValueCondition;
import org.ovirt.engine.ui.common.widget.label.BiosTypeLabel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.ClusterTypeLabel;
import org.ovirt.engine.ui.common.widget.label.ResiliencePolicyLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

public class ClusterGeneralModelForm extends AbstractModelBoundFormWidget<ClusterGeneralModel> {

    interface Driver extends UiCommonEditorDriver<ClusterGeneralModel, ClusterGeneralModelForm> {
    }

    StringValueLabel name = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel dataCenterName = new StringValueLabel();
    StringValueLabel cpuType = new StringValueLabel();
    BooleanLabel cpuThreads;
    PercentLabel<Integer> memoryOverCommit;
    ResiliencePolicyLabel resiliencePolicy;
    ClusterTypeLabel clusterType;
    BiosTypeRenderer biosTypeRenderer = new BiosTypeRenderer(AssetProvider.getConstants().autoDetect());
    BiosTypeLabel biosType = new BiosTypeLabel(biosTypeRenderer);
    StringValueLabel noOfVolumesTotal = new StringValueLabel();
    StringValueLabel noOfVolumesUp = new StringValueLabel();
    StringValueLabel noOfVolumesDown = new StringValueLabel();
    StringValueLabel compatibilityVersion = new StringValueLabel();
    StringValueLabel emulatedMachine = new StringValueLabel();
    StringValueLabel numberOfVms = new StringValueLabel();
    StringValueLabel clusterId = new StringValueLabel();

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public ClusterGeneralModelForm(ModelProvider<ClusterGeneralModel> modelProvider) {
        super(modelProvider, 3, 7);
    }

    /**
     * Initialize the form. Call this after ID has been set on the form,
     * so that form fields can use the ID as their prefix.
     */
    public void initialize() {
        cpuThreads = new BooleanLabel(constants.yes(), constants.no());
        memoryOverCommit = new PercentLabel<>();
        resiliencePolicy = new ResiliencePolicyLabel();
        clusterType = new ClusterTypeLabel();

        driver.initialize(this);

        DefaultValueCondition virtServiceNotSupported = () -> {
            boolean supportsVirtService = getModel().getEntity() != null
                    && getModel().getEntity().supportsVirtService();
            return !supportsVirtService;
        };

        DefaultValueCondition glusterServiceNotSupported = () -> {
            boolean supportsGlusterService = getModel().getEntity() != null
                    && getModel().getEntity().supportsGlusterService();
            return !supportsGlusterService;
        };

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
        boolean glusterSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly);

        formBuilder.addFormItem(new FormItem(constants.nameCluster(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionCluster(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.dcCluster(), dataCenterName, 2, 0, virtSupported));
        formBuilder.addFormItem(new FormItem(constants.compatibilityVersionCluster(), compatibilityVersion, 3, 0));

        // Show the cluster type only if the application is running in both the modes
        formBuilder.addFormItem(new FormItem(constants.clusterType(), clusterType, 4, 0, virtSupported
                && glusterSupported));


        formBuilder.addFormItem(new FormItem(constants.idCluster(), clusterId, 5, 0));
        // properties for virt support
        formBuilder.addFormItem(new FormItem(constants.cpuTypeCluster(), createCpuType(), 0, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.cpuThreadsCluster(), cpuThreads, 1, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.memoryOptimizationCluster(), memoryOverCommit, 2, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.resiliencePolicyCluster(), resiliencePolicy, 3, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.biosTypeGeneral(), biosType, 4, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.emulatedMachine(), emulatedMachine, 5, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.numberOfVmsCluster(), numberOfVms, 6, 1, virtSupported)
                .withDefaultValue(constants.notAvailableLabel(), virtServiceNotSupported));

        // properties for gluster support
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesTotalLabel(), noOfVolumesTotal, 0, 2, glusterSupported)
                .withDefaultValue(constants.notAvailableLabel(), glusterServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesUpLabel(), noOfVolumesUp, 1, 2, glusterSupported)
                .withDefaultValue(constants.notAvailableLabel(), glusterServiceNotSupported));
        formBuilder.addFormItem(new FormItem(constants.clusterVolumesDownLabel(), noOfVolumesDown, 2, 2, glusterSupported)
                .withDefaultValue(constants.notAvailableLabel(), glusterServiceNotSupported));
    }

    @Override
    protected void doEdit(ClusterGeneralModel model) {
        biosTypeRenderer.setArchitectureType(getModel().getArchitecture());
        getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(ARCHITECTURE_PROPERTY_CHANGE)) {
                    biosTypeRenderer.setArchitectureType(getModel().getArchitecture());
                    // change of the architecture changes the bios type rendering so we need to trigger the redraw
                    getModel().onPropertyChanged(EntityModel.ENTITY);
                }
            }
        });

        driver.edit(model);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private Widget createCpuType() {
        cpuType.getElement().getStyle().setWidth(90, Unit.PCT);
        cpuType.getElement().getStyle().setPaddingRight(5, Unit.PX);

        WidgetWithInfo cpuTypeWithInfo = new WidgetWithInfo(cpuType);
        updateCpuTypeInfo(cpuTypeWithInfo);

        getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(CPU_VERB_PROPERTY_CHANGE)) {
                    updateCpuTypeInfo(cpuTypeWithInfo);
                }
            }
        });
        return cpuTypeWithInfo;
    }

    private void updateCpuTypeInfo(WidgetWithInfo widgetWithInfo) {
        if (getModel().getCpuVerb() != null) {
            widgetWithInfo.setIconVisible(true);
            String cpuVerb = getModel().getCpuVerb().replace(",", ", ");//$NON-NLS-1$ //$NON-NLS-2$
            widgetWithInfo.setIconTooltipText(messages.clusterCpuTypeInfo(cpuVerb));
        } else {
            widgetWithInfo.setIconVisible(false);
        }
    }
}
