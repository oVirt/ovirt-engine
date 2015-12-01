package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NetworkProfilesModel extends ListModel<VnicProfileModel> {

    private EntityModel<Guid> dcId = new EntityModel<>();

    public EntityModel<Guid> getDcId() {
        return dcId;
    }

    public void updateDcId(Guid dcId) {
        for (VnicProfileModel profile : getItems()) {
            profile.initNetworkQoSList(dcId);
        }
        getDcId().setEntity(dcId);
    }

}
