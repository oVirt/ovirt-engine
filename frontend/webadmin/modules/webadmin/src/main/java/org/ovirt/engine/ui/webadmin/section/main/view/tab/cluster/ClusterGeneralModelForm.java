package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.FormItemWithDefaultValue;
import org.ovirt.engine.ui.common.widget.form.FormItemWithDefaultValue.Condition;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.ClusterTypeLabel;
import org.ovirt.engine.ui.common.widget.label.MemorySizeLabel;
import org.ovirt.engine.ui.common.widget.label.ResiliencePolicyLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class ClusterGeneralModelForm extends AbstractModelBoundFormWidget<ClusterGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<ClusterGeneralModel, ClusterGeneralModelForm> {
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel dataCenterName = new TextBoxLabel();
    TextBoxLabel cpuName = new TextBoxLabel();
    BooleanLabel cpuThreads;
    MemorySizeLabel<Integer> memoryOverCommit;
    ResiliencePolicyLabel resiliencePolicy;
    ClusterTypeLabel clusterType;
    TextBoxLabel noOfVolumesTotal = new TextBoxLabel();
    TextBoxLabel noOfVolumesUp = new TextBoxLabel();
    TextBoxLabel noOfVolumesDown = new TextBoxLabel();
    TextBoxLabel compatibilityVersion = new TextBoxLabel();

    private final Driver driver = GWT.create(Driver.class);

    public ClusterGeneralModelForm(ModelProvider<ClusterGeneralModel> modelProvider,
            final ApplicationConstants constants) {
        super(modelProvider, 3, 5);

        driver.initialize(this);

        Condition supportsVirtService = new Condition() {
            @Override
            public boolean isTrue() {
                return getModel().getEntity() != null && getModel().getEntity().supportsVirtService();
            }
        };

        Condition supportsGlusterService = new Condition() {
            @Override
            public boolean isTrue() {
                return getModel().getEntity() != null && getModel().getEntity().supportsGlusterService();
            }
        };

        String notAvailable = constants.notAvailableLabel();

        cpuThreads = new BooleanLabel(constants.yes(), constants.no());
        memoryOverCommit = new MemorySizeLabel<Integer>(constants);
        resiliencePolicy = new ResiliencePolicyLabel(constants);
        clusterType = new ClusterTypeLabel(constants);

        formBuilder.setColumnsWidth("180px", "180px", "180px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        formBuilder.addFormItem(new FormItem(constants.nameCluster(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionCluster(), description, 1, 0));
        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            formBuilder.addFormItem(new FormItem(constants.dcCluster(), dataCenterName, 2, 0));
        }
        formBuilder.addFormItem(new FormItem(constants.compatibilityVersionCluster(), compatibilityVersion, 3, 0));

        // Show the cluster type only if the application is running in both the modes
        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)
                && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly)) {
            formBuilder.addFormItem(new FormItem(constants.clusterType(), clusterType, 4, 0));
        }

        // properties for virt support
        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.cpuNameCluster(),
                    cpuName, 0, 1, notAvailable, supportsVirtService));
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.cpuThreadsCluster(),
                    cpuThreads, 1, 1, notAvailable, supportsVirtService));
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.memoryOptimizationCluster(),
                    memoryOverCommit, 2, 1, notAvailable, supportsVirtService));
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.resiliencePolicyCluster(),
                    resiliencePolicy, 3, 1, notAvailable, supportsVirtService));
        }

        // properties for gluster support
        if (ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly)) {
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.clusterVolumesTotalLabel(),
                    noOfVolumesTotal, 0, 2, notAvailable, supportsGlusterService));
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.clusterVolumesUpLabel(),
                    noOfVolumesUp, 1, 2, notAvailable, supportsGlusterService));
            formBuilder.addFormItem(new FormItemWithDefaultValue(constants.clusterVolumesDownLabel(),
                    noOfVolumesDown, 2, 2, notAvailable, supportsGlusterService));
        }
    }

    @Override
    protected void doEdit(ClusterGeneralModel model) {
        driver.edit(model);
    }

}
