package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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

            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object ReturnValue) {
                    List<DiskImage> disks = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                    Collections.sort(disks, new DiskByDiskAliasComparator());

                    setItems(disks);
                }
            };

            IdQueryParameters queryParameters = new IdQueryParameters(vm.getId());
            queryParameters.setRefresh(getIsQueryFirstTime());
            Frontend.getInstance().runQuery(VdcQueryType.GetAllDisksPartialDataByVmId, queryParameters,
                    _asyncQuery);
        }
        else if (getEntity() instanceof VmPool) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result) {
                    if (result != null) {
                        VM vm = ((VdcQueryReturnValue) result).getReturnValue();
                        if (vm == null) {
                            return;
                        }
                        VmBasicDiskListModel poolDiskListModel = (VmBasicDiskListModel) model;

                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(poolDiskListModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model1, Object ReturnValue) {
                                List<DiskImage> disks = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                Collections.sort(disks, new DiskByDiskAliasComparator());

                                setItems(disks);
                            }
                        };
                        IdQueryParameters queryParameters = new IdQueryParameters(vm.getId());
                        queryParameters.setRefresh(getIsQueryFirstTime());
                        Frontend.getInstance().runQuery(VdcQueryType.GetAllDisksPartialDataByVmId, queryParameters,
                                _asyncQuery1);
                    }

                }
            };

            VmPool pool = (VmPool) getEntity();
            Frontend.getInstance().runQuery(VdcQueryType.GetVmDataByPoolId,
                    new IdQueryParameters(pool.getVmPoolId()),
                    _asyncQuery);
        }
    }

    @Override
    protected String getListName() {
        return "VmBasicDiskListModel"; //$NON-NLS-1$
    }

}
