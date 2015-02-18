package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CoreVmBaseToUnitBuilder extends CompositeBuilder<VmBase, UnitVmModel> {

    private boolean everyFeatureSupported = false;

    public CoreVmBaseToUnitBuilder(Builder<VmBase, UnitVmModel>... builders) {
        super(builders);
    }

    public CoreVmBaseToUnitBuilder() {
        this(
                new KernelParamsVmBaseToUnitBuilder(),
                new SerialNumberPolicyVmBaseToUnitBuilder()
        );
    }

    public CoreVmBaseToUnitBuilder withEveryFeatureSupported() {
        this.everyFeatureSupported = true;

        return this;
    }

    @Override
    protected void postBuild(VmBase vm, UnitVmModel model) {
        if (supported(ConfigurationValues.BootMenuSupported, model)) {
            model.getBootMenuEnabled().setEntity(vm.isBootMenuEnabled());
        }

        model.getVncKeyboardLayout().setSelectedItem(vm.getVncKeyboardLayout());
        model.getIsDeleteProtected().setEntity(vm.isDeleteProtected());
        model.selectSsoMethod(vm.getSsoMethod());

        if (supported(ConfigurationValues.SpiceFileTransferToggleSupported, model)) {
            model.getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        }

        if (supported(ConfigurationValues.SpiceCopyPasteToggleSupported, model)) {
            model.getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());
        }

        if (supported(ConfigurationValues.AutoConvergenceSupported, model)) {
            model.getAutoConverge().setSelectedItem(vm.getAutoConverge());
        }

        if (supported(ConfigurationValues.MigrationCompressionSupported, model)) {
            model.getMigrateCompressed().setSelectedItem(vm.getMigrateCompressed());
        }
    }

    protected boolean supported(ConfigurationValues feature, UnitVmModel model) {
        return everyFeatureSupported || AsyncDataProvider.getInstance().supportedForUnitVmModel(feature, model);
    }
}
