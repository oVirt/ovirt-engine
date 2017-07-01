package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.Collection;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditVnicProfileModel extends VnicProfileModel {

    public EditVnicProfileModel(IModel sourceModel,
            VnicProfile profile,
            Guid dcId,
            boolean customPropertiesVisible) {
        super(sourceModel, customPropertiesVisible, dcId, profile.getNetworkQosId());
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHelpTag(HelpTag.edit_vnic_profile);
        setHashName("edit_vnic_profile"); //$NON-NLS-1$

        setProfile(profile);

        getName().setEntity(profile.getName());
        getDescription().setEntity(profile.getDescription());
        getPassthrough().setEntity(getProfile().isPassthrough());
        getMigratable().setEntity(!getProfile().isPassthrough() || getProfile().isMigratable());
        getPortMirroring().setEntity(getProfile().isPortMirroring());
        getPublicUse().setIsAvailable(false);

        updateChangabilityIfVmsUsingTheProfile();
    }

    public EditVnicProfileModel(IModel sourceModel, VnicProfile profile, Guid dcId) {
        this(sourceModel, profile, dcId, true);
    }

    public EditVnicProfileModel(VnicProfile profile) {
        this(null, profile, null, false);
    }

    @Override
    protected void initCustomProperties() {
        getCustomPropertySheet().deserialize(KeyValueModel
                .convertProperties(getProfile().getCustomProperties()));
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.UpdateVnicProfile;
    }

    private void updateChangabilityIfVmsUsingTheProfile() {
        IdQueryParameters params =
                new IdQueryParameters(getProfile().getId());
        startProgress();
        Frontend.getInstance().runQuery(QueryType.GetVmsByVnicProfileId, params,
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    Collection<VM> vms = returnValue.getReturnValue();
                    if (vms != null && !vms.isEmpty()) {
                        getPortMirroring().setChangeProhibitionReason(ConstantsManager.getInstance()
                                .getConstants()
                                .portMirroringNotChangedIfUsedByVms());
                        getPortMirroring().setIsChangeable(false);

                        getPassthrough().setChangeProhibitionReason(ConstantsManager.getInstance()
                                .getConstants()
                                .passthroughNotChangedIfUsedByVms());
                        getPassthrough().setIsChangeable(false);
                    }
                    stopProgress();
                }));
    }

    @Override
    protected void initSelectedNetworkFilter() {
        getNetworkFilter().setSelectedItem(Linq.firstOrNull(getNetworkFilter().getItems(),
                new Linq.IdPredicate<>(getProfile().getNetworkFilterId())));
    }

    @Override
    protected void updateNetworks(Collection<Network> networks) {
        Network currentNetwork =
                findNetwork(getProfile().getNetworkId(), networks);
        getNetwork().setSelectedItem(currentNetwork);
        getNetwork().setIsChangeable(false);
    }

    private Network findNetwork(Guid networkId, Iterable<Network> networks) {
        for (Network network : networks) {
            if (networkId.equals(network.getId())) {
                return network;
            }
        }
        return null;
    }
}
