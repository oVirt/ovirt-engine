package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PoolInterfaceListModel extends SearchableListModel<VmPool, VmNetworkInterface> {
    public PoolInterfaceListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHelpTag(HelpTag.network_interfaces);
        setHashName("network_interfaces"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        VmPool pool = getEntity();
        if (pool != null) {
            Frontend.getInstance().runQuery(QueryType.GetVmDataByPoolId,
                    new IdQueryParameters(pool.getVmPoolId()),
                    new AsyncQuery<QueryReturnValue>(result -> {
                        if (result != null) {
                            VM vm = result.getReturnValue();
                            if (vm == null) {
                                return;
                            }
                            syncSearch(QueryType.GetVmInterfacesByVmId, new IdQueryParameters(vm.getId()));
                        }
                    }));
        }
    }

    @Override
    protected String getListName() {
        return "PoolInterfaceListModel"; //$NON-NLS-1$
    }
}
