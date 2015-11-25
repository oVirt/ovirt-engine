package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AddProviderModel extends ProviderModel {

    public AddProviderModel(ProviderListModel sourceListModel) {
        super(sourceListModel, VdcActionType.AddProvider, new Provider());
        setTitle(ConstantsManager.getInstance().getConstants().addProviderTitle());
        setHelpTag(HelpTag.add_provider);
        setHashName("add_provider"); //$NON-NLS-1$

        getType().setSelectedItem(Linq.firstOrNull((Iterable<ProviderType>) getType().getItems()));

        getNeutronAgentModel().init(provider); // this is okay because AdditionalProperties == null at this point
    }

    @Override
    protected void updateDatacentersForVolumeProvider() {
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                ProviderModel providerModel = (ProviderModel) model;

                // Filter-out unsupported DCs
                Collection<StoragePool> supportedDataCenters = Linq.where(dataCenters,
                        new Linq.IPredicate<StoragePool>() {
                            @Override
                            public boolean match(StoragePool storagePool) {
                                return (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                                        ConfigurationValues.CinderProviderSupported,
                                        storagePool.getCompatibilityVersion().toString());
                            }
                        });

                // add an empty DataCenter to the list
                StoragePool noneStoragePool = new StoragePool();
                noneStoragePool.setId(Guid.Empty);
                noneStoragePool.setName("(none)"); //$NON-NLS-1$
                supportedDataCenters.add(noneStoragePool);

                providerModel.getDataCenter().setItems(supportedDataCenters);
            }
        }));
    }
}
