package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class NewVnicProfileModel extends VnicProfileModel {

    public NewVnicProfileModel(SearchableListModel<?, ?> sourceModel,
            boolean customPropertiesVisible,
            Guid dcId) {
        super(sourceModel, customPropertiesVisible, dcId, null);
        setTitle(constants.vnicProfileTitle());
        setHelpTag(HelpTag.new_vnic_profile);
        setHashName("new_vnic_profile"); //$NON-NLS-1$
        getPortMirroring().setEntity(false);
        getPassthrough().setEntity(false);
        getMigratable().setEntity(true);
    }

    public NewVnicProfileModel(SearchableListModel<?, ?> sourceModel, Guid dcId) {
        this(sourceModel, true, dcId);
    }

    public NewVnicProfileModel() {
        this(null, false, null);
    }

    @Override
    protected void initCustomProperties() {
        // Do nothing
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.AddVnicProfile;
    }

    @Override
    protected ActionParametersBase getActionParameters() {
        AddVnicProfileParameters parameters = new AddVnicProfileParameters(getProfile());
        parameters.setPublicUse(getPublicUse().getEntity());
        return parameters;
    }

    @Override
    protected void initSelectedNetworkFilter() {
        getNetworkFilter().setSelectedItem(Linq.firstOrNull(getNetworkFilter().getItems(),
                new Linq.NamePredicate(NetworkFilter.VDSM_NO_MAC_SPOOFING)));
    }

    @Override
    protected void initSelectedFailoverProfile() {
        getFailoverVnicProfile().setSelectedItem(EMPTY_FAILOVER_VNIC_PROFILE);
    }

    @Override
    protected void updateChangeabilityIfVmsUsingTheProfile() {
        // Do nothing
    }
}
