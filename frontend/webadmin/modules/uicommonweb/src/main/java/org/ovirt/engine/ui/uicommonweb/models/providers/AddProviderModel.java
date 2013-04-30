package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AddProviderModel extends ProviderModel {

    public AddProviderModel(ProviderListModel sourceListModel) {
        super(sourceListModel, VdcActionType.AddProvider, new Provider());
        setTitle(ConstantsManager.getInstance().getConstants().addProviderTitle());
        setHashName("add_provider"); //$NON-NLS-1$
    }

}
