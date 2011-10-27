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
			Model model = (Model)item;

			storage_domains isoStorage = DataProvider.GetIsoDomainByDataCenterId(dataCenter.getId());
			storage_domains exportStorage = DataProvider.GetExportDomainByDataCenterId(dataCenter.getId());

			// available type/function items are:
			// all in case of Unassigned DC.
			// ISO in case the specified DC doesn't have an attached ISO domain.
			// Export in case the specified DC doesn't have an attached export domain.
			model.setIsSelectable((dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) || (item.getRole() == StorageDomainType.ISO && isoStorage == null) || (item.getRole() == StorageDomainType.ImportExport && exportStorage == null)));
		}
	}
}