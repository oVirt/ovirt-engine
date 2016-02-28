package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.vms.ProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

public abstract class VmInstanceTypeManager extends InstanceTypeManager {

    public VmInstanceTypeManager(UnitVmModel model) {
        super(model);
    }

    @Override
    protected void doUpdateManagedFieldsFrom(VmBase vmBase) {
        super.doUpdateManagedFieldsFrom(vmBase);

        if (vmBase == null) {
            return;
        }

        updateNetworkInterfacesByTemplate(vmBase);
    }

    public void updateNetworkInterfacesByTemplate(VmBase vmBase) {

        VdcQueryType queryType = (vmBase instanceof VmTemplate) ? VdcQueryType.GetTemplateInterfacesByTemplateId : VdcQueryType.GetVmInterfacesByVmId;

        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object parentModel, Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                List<VmNetworkInterface> nics = ((VdcQueryReturnValue) returnValue).getReturnValue();
                updateNetworkInterfaces(getNetworkProfileBehavior(), nics);
            }
        });

        Frontend.getInstance().runQuery(queryType,
                new IdQueryParameters(vmBase.getId()),
                query);
    }

    private void updateNetworkInterfaces(final ProfileBehavior behavior, final List<VmNetworkInterface> argNics) {
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object parentModel, Object returnValue) {
                List<VnicProfileView> profiles = (List<VnicProfileView>) returnValue;
                List<VnicInstanceType> vnicInstanceTypes = new ArrayList<>();
                List<VmNetworkInterface> nics = (argNics == null) ? new ArrayList<VmNetworkInterface>() : argNics;

                for (VmNetworkInterface nic : nics) {
                    final VnicInstanceType vnicInstanceType = new VnicInstanceType(nic);
                    vnicInstanceType.setItems(profiles);
                    behavior.initSelectedProfile(vnicInstanceType, vnicInstanceType.getNetworkInterface());
                    vnicInstanceTypes.add(vnicInstanceType);
                }

                getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(profiles);
                getModel().getNicsWithLogicalNetworks().setItems(vnicInstanceTypes);
                getModel().getNicsWithLogicalNetworks().setSelectedItem(Linq.firstOrNull(vnicInstanceTypes));
            }
        });

        behavior.initProfiles(getModel().getSelectedCluster().getId(), getModel().getSelectedDataCenter().getId(), query);
    }

    protected abstract ProfileBehavior getNetworkProfileBehavior();
}
