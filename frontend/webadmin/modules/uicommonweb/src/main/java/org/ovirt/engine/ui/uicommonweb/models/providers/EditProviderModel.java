package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditProviderModel extends ProviderModel {

    public EditProviderModel(SearchableListModel sourceListModel, Provider provider) {
        super(sourceListModel, VdcActionType.UpdateProvider, provider);
        setTitle(ConstantsManager.getInstance().getConstants().editProviderTitle());
        setHashName("edit_provider"); //$NON-NLS-1$
    }

}
