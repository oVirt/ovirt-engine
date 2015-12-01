package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class VnicInstancesModel extends ListModel<VnicInstanceType> {

    private final ListModel<VnicProfileView> vnicProfiles = new ListModel<>();

    public ListModel<VnicProfileView> getVnicProfiles() {
        return vnicProfiles;
    }

}
