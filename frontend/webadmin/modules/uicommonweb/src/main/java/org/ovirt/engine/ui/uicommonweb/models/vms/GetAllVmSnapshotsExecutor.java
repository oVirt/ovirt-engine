package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("unused")
public class GetAllVmSnapshotsExecutor implements IFrontendMultipleQueryAsyncCallback
{
    private Guid vmId = new Guid();
    private AsyncQuery query;
    private java.util.List<DiskImage> disks;
    private boolean isRefresh;

    public GetAllVmSnapshotsExecutor(Guid vmId, AsyncQuery query, boolean isRefresh)
    {
        this.vmId = vmId;
        this.query = query;
        this.isRefresh = isRefresh;
    }

    public void Execute()
    {
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        GetAllVmSnapshotsExecutor model = (GetAllVmSnapshotsExecutor) target;
                        model.PostExecute(returnValue);

                    }
                }), vmId, isRefresh);
    }

    public void PostExecute(Object returnValue)
    {
        disks = Linq.<DiskImage> Cast((Iterable) returnValue);

        java.util.ArrayList<VdcQueryType> queryTypes = new java.util.ArrayList<VdcQueryType>();
        java.util.ArrayList<VdcQueryParametersBase> parameters = new java.util.ArrayList<VdcQueryParametersBase>();

        for (DiskImage disk : disks)
        {
            queryTypes.add(VdcQueryType.GetAllVmSnapshotsByDrive);
            parameters.add(new GetAllVmSnapshotsByDriveParameters(vmId, disk.getinternal_drive_mapping()));
        }

        Frontend.RunMultipleQueries(queryTypes, parameters, this);
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        java.util.ArrayList<AsyncDataProvider.GetSnapshotListQueryResult> list =
                new java.util.ArrayList<AsyncDataProvider.GetSnapshotListQueryResult>();

        for (int i = 0; i < result.getReturnValues().size(); i++)
        {
            GetAllVmSnapshotsByDriveQueryReturnValue returnValue =
                    (GetAllVmSnapshotsByDriveQueryReturnValue) result.getReturnValues().get(i);

            AsyncDataProvider.GetSnapshotListQueryResult tempVar =
                    new AsyncDataProvider.GetSnapshotListQueryResult(returnValue.getTryingImage(),
                            Linq.<DiskImage> Cast((Iterable) returnValue.getReturnValue()),
                            disks.get(i));
            tempVar.setVmId(vmId);
            list.add(tempVar);
        }

        query.asyncCallback.OnSuccess(query.getModel(), list);
    }
}
