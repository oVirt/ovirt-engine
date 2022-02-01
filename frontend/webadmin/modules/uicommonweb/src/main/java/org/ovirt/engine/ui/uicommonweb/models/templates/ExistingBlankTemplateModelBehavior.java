package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MultiQueuesVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingNonClusterModelBehavior;

public class ExistingBlankTemplateModelBehavior extends ExistingNonClusterModelBehavior {

    private VmTemplate template;

    public ExistingBlankTemplateModelBehavior(VmTemplate template) {
        super(template);
        this.template = template;
    }

    @Override
    public void initialize() {
        super.initialize();

        getModel().getBaseTemplate().setIsAvailable(false);
        getModel().getTemplateVersionName().setIsAvailable(false);
        getModel().getVmType().setIsChangeable(true);
        getModel().getEmulatedMachine().setIsAvailable(false);
        getModel().getCustomCpu().setIsAvailable(false);
        getModel().getCustomCompatibilityVersion().setIsAvailable(false);
        getModel().getOSType().setIsAvailable(false);
        getModel().getIsSealed().setIsAvailable(false);

        updateCustomPropertySheet(latestCluster());
    }

    @Override
    protected void postBuild() {
        getModel().getIsSealed().setEntity(template.isSealed());
        updateTimeZone(template.getTimeZone());
        getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);
        getModel().getVmInitModel().init(template);
    }

    @Override
    protected Version getCompatibilityVersion() {
        return latestCluster();
    }

    @Override
    protected void buildModel(VmBase vmBase, BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                new NameAndDescriptionVmBaseToUnitBuilder(),
                new CommentVmBaseToUnitBuilder(),
                new CommonVmBaseToUnitBuilder(),
                new MultiQueuesVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    public VmTemplate getVmTemplate() {
        return template;
    }
}
