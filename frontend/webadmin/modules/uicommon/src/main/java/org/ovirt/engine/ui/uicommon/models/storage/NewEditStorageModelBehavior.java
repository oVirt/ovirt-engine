package org.ovirt.engine.ui.uicommon.models.storage;
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

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class NewEditStorageModelBehavior extends StorageModelBehavior
{
	@Override
	public void UpdateItemsAvailability()
	{
		super.UpdateItemsAvailability();

		storage_pool dataCenter = (storage_pool)getModel().getDataCenter().getSelectedItem();

		//Allow Data storage type corresponding to the selected data-center type + ISO and Export that are NFS only:
		for (IStorageModel item : Linq.<IStorageModel>Cast(getModel().getItems()))
		{
			Model model = (Model)item;

			model.setIsSelectable(dataCenter != null && ((dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) && item.getRole() == StorageDomainType.Data) || (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) && ((item.getRole() == StorageDomainType.Data && item.getType() == dataCenter.getstorage_pool_type()) || (item.getRole() == StorageDomainType.ImportExport && item.getType() == StorageType.NFS && dataCenter.getstatus() != StoragePoolStatus.Uninitialized && DataProvider.GetExportDomainByDataCenterId(dataCenter.getId()) == null) || item.getRole() == StorageDomainType.ISO && item.getType() == StorageType.NFS && dataCenter.getstatus() != StoragePoolStatus.Uninitialized && DataProvider.GetIsoDomainByDataCenterId(dataCenter.getId()) == null)) || (getModel().getStorage() != null && item.getType() == getModel().getStorage().getstorage_type())));
		}
	}
}