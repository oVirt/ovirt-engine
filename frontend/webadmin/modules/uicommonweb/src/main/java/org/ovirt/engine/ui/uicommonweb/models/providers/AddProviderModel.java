package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AddProviderModel extends ProviderModel {

    private static final String QPID_PORT_DEFAULT = "5672"; //$NON-NLS-1$

    @SuppressWarnings("unchecked")
    public AddProviderModel(ProviderListModel sourceListModel) {
        super(sourceListModel, VdcActionType.AddProvider, new Provider());
        setTitle(ConstantsManager.getInstance().getConstants().addProviderTitle());
        setHashName("add_provider"); //$NON-NLS-1$

        getType().setSelectedItem(Linq.firstOrDefault((Iterable<ProviderType>) getType().getItems()));
        getRequiresAuthentication().setEntity(false);

        getQpidPort().setEntity(QPID_PORT_DEFAULT);
    }

}
