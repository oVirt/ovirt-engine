package org.ovirt.engine.ui.uicommon.models.vms;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class GetAllVmSnapshotsExecutor implements IFrontendMultipleQueryAsyncCallback
{
	private Guid vmId = new Guid();
	private AsyncQuery query;
	private java.util.List<DiskImage> disks;

	public GetAllVmSnapshotsExecutor(Guid vmId, AsyncQuery query)
	{
		this.vmId = vmId;
		this.query = query;
	}

	public void Execute()
	{
		AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			GetAllVmSnapshotsExecutor model = (GetAllVmSnapshotsExecutor)target;
			model.PostExecute(returnValue);

			}
		}), vmId);
	}

	public void PostExecute(Object returnValue)
	{
		disks = Linq.<DiskImage>Cast((Iterable)returnValue);

		java.util.ArrayList<VdcQueryType> queryTypes = new java.util.ArrayList<VdcQueryType>();
		java.util.ArrayList<VdcQueryParametersBase> parameters = new java.util.ArrayList<VdcQueryParametersBase>();

		for (DiskImage disk : disks)
		{
			queryTypes.add(VdcQueryType.GetAllVmSnapshotsByDrive);
			parameters.add(new GetAllVmSnapshotsByDriveParameters(vmId, disk.getinternal_drive_mapping()));
		}

		Frontend.RunMultipleQueries(queryTypes, parameters, this);
	}

	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		java.util.ArrayList<AsyncDataProvider.GetSnapshotListQueryResult> list = new java.util.ArrayList<AsyncDataProvider.GetSnapshotListQueryResult>();

		for (int i = 0; i < result.getReturnValues().size(); i++)
		{
			GetAllVmSnapshotsByDriveQueryReturnValue returnValue = (GetAllVmSnapshotsByDriveQueryReturnValue)result.getReturnValues().get(i);

			AsyncDataProvider.GetSnapshotListQueryResult tempVar = new AsyncDataProvider.GetSnapshotListQueryResult(returnValue.getTryingImage(), Linq.<DiskImage>Cast((Iterable)returnValue.getReturnValue()), disks.get(i));
			tempVar.setVmId(vmId);
			list.add(tempVar);
		}

		query.asyncCallback.OnSuccess(query.getModel(), list);
	}
}