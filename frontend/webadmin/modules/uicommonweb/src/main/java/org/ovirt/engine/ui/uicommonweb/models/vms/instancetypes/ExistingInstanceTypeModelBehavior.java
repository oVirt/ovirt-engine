package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

public class ExistingInstanceTypeModelBehavior extends ExistingNonClusterModelBehavior {

    private VmTemplate instanceType;

    public ExistingInstanceTypeModelBehavior(VmTemplate instanceType) {
        super(instanceType);
        this.instanceType = instanceType;
    }

    @Override
    protected void postBuild() {
        AsyncQuery getVmNicsQuery = new AsyncQuery();
        getVmNicsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<VnicProfileView> profiles = new ArrayList<>(Arrays.asList(VnicProfileView.EMPTY));
                List<VnicInstanceType> vnicInstanceTypes = new ArrayList<>();

                for (VmNetworkInterface nic : (List<VmNetworkInterface>) result) {
                    final VnicInstanceType vnicInstanceType = new VnicInstanceType(nic);
                    vnicInstanceType.setItems(profiles);
                    vnicInstanceType.setSelectedItem(VnicProfileView.EMPTY);
                    vnicInstanceTypes.add(vnicInstanceType);
                }

                getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(profiles);
                getModel().getNicsWithLogicalNetworks().setItems(vnicInstanceTypes);
                getModel().getNicsWithLogicalNetworks().setSelectedItem(Linq.firstOrNull(vnicInstanceTypes));
            }
        };
        AsyncDataProvider.getInstance().getTemplateNicList(getVmNicsQuery, instanceType.getId());
    }
}
