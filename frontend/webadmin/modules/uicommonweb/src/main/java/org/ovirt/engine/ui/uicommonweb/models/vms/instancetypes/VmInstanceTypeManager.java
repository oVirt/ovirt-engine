package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

import java.util.ArrayList;
import java.util.List;

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
        boolean hotUpdateSupported =
                (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.NetworkLinkingSupported,
                        getModel().getSelectedCluster().getcompatibility_version().toString());

        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object parentModel, Object returnValue) {
                List<VnicProfileView> profiles = (List<VnicProfileView>) returnValue;
                List<VnicInstanceType> vnicInstanceTypes = new ArrayList<VnicInstanceType>();
                List<VmNetworkInterface> nics = (argNics == null) ? new ArrayList<VmNetworkInterface>() : argNics;

                for (VmNetworkInterface nic : nics) {
                    final VnicInstanceType vnicInstanceType = new VnicInstanceType(nic);
                    vnicInstanceType.setItems(profiles);
                    behavior.initSelectedProfile(vnicInstanceType, vnicInstanceType.getNetworkInterface());
                    vnicInstanceTypes.add(vnicInstanceType);
                }

                getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(profiles);
                getModel().getNicsWithLogicalNetworks().setItems(vnicInstanceTypes);
                getModel().getNicsWithLogicalNetworks().setSelectedItem(Linq.firstOrDefault(vnicInstanceTypes));
            }
        });

        behavior.initProfiles(hotUpdateSupported, getModel().getSelectedCluster().getId(), getModel().getSelectedDataCenter().getId(), query);
    }

    protected abstract ProfileBehavior getNetworkProfileBehavior();
}
