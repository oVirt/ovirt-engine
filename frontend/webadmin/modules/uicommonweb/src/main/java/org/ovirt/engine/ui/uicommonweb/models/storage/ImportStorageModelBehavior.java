package org.ovirt.engine.ui.uicommonweb.models.storage;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class ImportStorageModelBehavior extends StorageModelBehavior
{
	@Override
	public java.util.List<storage_pool> FilterDataCenter(java.util.List<storage_pool> source)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
		return Linq.ToList(Linq.Where(source, new Linq.DataCenterStatusPredicate(StoragePoolStatus.Up)));
	}

	@Override
	public void UpdateItemsAvailability()
	{
		super.UpdateItemsAvailability();

		storage_pool dataCenter = (storage_pool)getModel().getDataCenter().getSelectedItem();

		for (IStorageModel item : Linq.<IStorageModel>Cast(getModel().getItems()))
		{
			if (item.getRole() == StorageDomainType.ISO)
			{
				AsyncDataProvider.GetIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

					Object[] array = (Object[])target;
					ImportStorageModelBehavior behavior = (ImportStorageModelBehavior)array[0];
					IStorageModel storageModelItem = (IStorageModel)array[1];
					behavior.PostUpdateItemsAvailability(behavior, storageModelItem, returnValue == null);

			}
		}), dataCenter.getId());
			}
			else if (item.getRole() == StorageDomainType.ImportExport)
			{
				AsyncDataProvider.GetExportDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

					Object[] array = (Object[])target;
					ImportStorageModelBehavior behavior = (ImportStorageModelBehavior)array[0];
					IStorageModel storageModelItem = (IStorageModel)array[1];
					behavior.PostUpdateItemsAvailability(behavior, storageModelItem, returnValue == null);

			}
		}), dataCenter.getId());
			}
			else
			{
				PostUpdateItemsAvailability(this, item, false);
			}
		}
	}

	public void PostUpdateItemsAvailability(ImportStorageModelBehavior behavior, IStorageModel item, boolean isNoStorageAttached)
	{
		Model model = (Model)item;
		storage_pool dataCenter = (storage_pool)getModel().getDataCenter().getSelectedItem();

		// available type/function items are:
		// all in case of Unassigned DC.
		// ISO in case the specified DC doesn't have an attached ISO domain.
		// Export in case the specified DC doesn't have an attached export domain.
		model.setIsSelectable((dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) || (item.getRole() == StorageDomainType.ISO && isNoStorageAttached) || (item.getRole() == StorageDomainType.ImportExport && isNoStorageAttached)));

		behavior.OnStorageModelUpdated(item);
	}
}