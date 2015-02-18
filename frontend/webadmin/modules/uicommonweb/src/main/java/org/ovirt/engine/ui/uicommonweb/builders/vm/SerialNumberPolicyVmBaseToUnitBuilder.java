package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class SerialNumberPolicyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {

    private boolean everyFeatureSupported = false;

    public SerialNumberPolicyVmBaseToUnitBuilder withEveryFeatureSupported() {
        everyFeatureSupported = true;

        return this;
    }

    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        if (supported(ConfigurationValues.SerialNumberPolicySupported, model)) {
            model.getSerialNumberPolicy().setSelectedSerialNumberPolicy(vm.getSerialNumberPolicy());
            model.getSerialNumberPolicy().getCustomSerialNumber().setEntity(vm.getCustomSerialNumber());
        }
    }

    protected boolean supported(ConfigurationValues feature, UnitVmModel model) {
        return everyFeatureSupported || AsyncDataProvider.getInstance().supportedForUnitVmModel(feature, model);
    }
}
