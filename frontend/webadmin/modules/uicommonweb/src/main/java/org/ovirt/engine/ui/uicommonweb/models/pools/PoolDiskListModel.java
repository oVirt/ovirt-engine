package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModelBase;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class PoolDiskListModel extends VmDiskListModelBase<VmPool> {
    private VM vm;

    public VM getVM() {
        return vm;
    }

    public void setVM(VM vm) {
        this.vm = vm;
    }

    public PoolDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHelpTag(HelpTag.disks);
        setHashName("disks"); //$NON-NLS-1$
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

                            setVM(vm);
                            syncSearch();
                        }
                    }));
        }
    }

    @Override
    protected void syncSearch() {
        if (getVM() == null) {
            return;
        }

        super.syncSearch(QueryType.GetAllDisksByVmId, new IdQueryParameters(getVM().getId()));
    }

    @Override
    protected String getListName() {
        return "PoolDiskListModel"; //$NON-NLS-1$
    }
}
