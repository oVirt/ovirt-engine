package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class VmBasicDiskListModel extends SearchableListModel<Object, DiskImage> {
    public VmBasicDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        if (getEntity() instanceof VM) {
            VM vm = (VM) getEntity();

            IdQueryParameters queryParameters = new IdQueryParameters(vm.getId());
            queryParameters.setRefresh(getIsQueryFirstTime());
            Frontend.getInstance().runQuery(VdcQueryType.GetAllDisksPartialDataByVmId, queryParameters,
                    new SetSortedItemsAsyncQuery(new DiskByDiskAliasComparator()));
        }
        else if (getEntity() instanceof VmPool) {
            VmPool pool = (VmPool) getEntity();

            Frontend.getInstance().runQuery(VdcQueryType.GetVmDataByPoolId,
                    new IdQueryParameters(pool.getVmPoolId()),
                    new AsyncQuery<>(new AsyncCallback<VdcQueryReturnValue>() {
                        @Override
                        public void onSuccess(VdcQueryReturnValue result) {
                            if (result != null) {
                                VM vm = result.getReturnValue();
                                if (vm == null) {
                                    return;
                                }

                                IdQueryParameters queryParameters = new IdQueryParameters(vm.getId());
                                queryParameters.setRefresh(getIsQueryFirstTime());
                                Frontend.getInstance().runQuery(VdcQueryType.GetAllDisksPartialDataByVmId, queryParameters,
                                        new SetSortedItemsAsyncQuery(new DiskByDiskAliasComparator()));
                            }

                        }
                    }));
        }
    }

    @Override
    protected String getListName() {
        return "VmBasicDiskListModel"; //$NON-NLS-1$
    }

}
