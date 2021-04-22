package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.Collection;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;

public class EditVnicProfileModel extends VnicProfileModel {

    public EditVnicProfileModel(SearchableListModel<?, ?> sourceModel,
            VnicProfile profile,
            Guid dcId,
            boolean customPropertiesVisible) {
        super(sourceModel, customPropertiesVisible, dcId, profile.getNetworkQosId());
        setTitle(constants.vnicProfileTitle());
        setHelpTag(HelpTag.edit_vnic_profile);
        setHashName("edit_vnic_profile"); //$NON-NLS-1$

        setProfile(profile);

        getName().setEntity(profile.getName());
        getDescription().setEntity(profile.getDescription());
        getPassthrough().setEntity(getProfile().isPassthrough());
        getMigratable().setEntity(!getProfile().isPassthrough() || getProfile().isMigratable());
        getPortMirroring().setEntity(getProfile().isPortMirroring());
        getPublicUse().setIsAvailable(false);
    }

    public EditVnicProfileModel(SearchableListModel<?, ?> sourceModel, VnicProfile profile, Guid dcId) {
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

    protected void updateChangeabilityIfVmsUsingTheProfile() {
        startProgress();
        Frontend.getInstance().runQuery(QueryType.GetVmsByVnicProfileId,
                new IdQueryParameters(getProfile().getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    Collection<VM> vms = returnValue.getReturnValue();
                    if (vms != null && !vms.isEmpty()) {
                        getPortMirroring().setIsChangeable(false, constants.portMirroringNotChangedIfUsedByVms());
                        getPassthrough().setIsChangeable(false, constants.passthroughNotChangedIfUsedByVms());
                        getFailoverVnicProfile().setIsChangeable(false, constants.failoverNotChangedIfUsedByVms());
                    }
                    stopProgress();
                }));
    }

    @Override
    protected void initSelectedFailoverProfile() {
        getFailoverVnicProfile().setSelectedItem(Linq.firstOrNull(getFailoverVnicProfile().getItems(),
                new Linq.IdPredicate<>(getProfile().getFailoverVnicProfileId())));
    }

    @Override
    protected void initSelectedNetworkFilter() {
        getNetworkFilter().setSelectedItem(Linq.firstOrNull(getNetworkFilter().getItems(),
                new Linq.IdPredicate<>(getProfile().getNetworkFilterId())));
    }
}
