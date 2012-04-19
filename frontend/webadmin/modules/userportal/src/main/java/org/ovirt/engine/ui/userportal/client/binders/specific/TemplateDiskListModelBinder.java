package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelBinder;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class TemplateDiskListModelBinder  implements ListModelBinder {

	ListModel model;

	RendererType rendererType = RendererType.Grid;
	
	@Override
	public void setModel(Model model) {
		this.model = (ListModel)model;
	}
	
	@Override
	public ListGridField[] getFields() {
		return fields;
	}

	@Override
	public RecordList calcRecords() {
		RecordList records = new RecordList();

		Iterable<DiskImage> disks = (Iterable)model.getItems();

		Translator translator = EnumTranslator.Create(VolumeType.class);
		
		if (disks != null) {
			for (DiskImage disk : disks) {
				ListGridRecord r = new ListGridRecord();
				r.setAttribute("name", "Disk " + disk.getinternal_drive_mapping());
				r.setAttribute("size", (disk.getsize() / (1024*1024*1024)) + "GB");
				r.setAttribute("format", disk.getvolume_format().toString());
				r.setAttribute("allocation", translator.get(disk.getvolume_type()));
				r.setAttribute("interface", disk.getDiskInterface().toString());
				r.setAttribute("dateCreated", UserPortalTools.formatDate(disk.getcreation_date()));
				r.setAttribute("entity", disk);
				r.setAttribute("entityGuid", disk.getId());
				records.add(r);
			}
		}
		return records;
	}

	private static ListGridField[] fields = {
		new ListGridField("name", "Name", 120),
		new ListGridField("size", "Size", 120),
		new ListGridField("format", "Format", 120),
		new ListGridField("allocation", "Allocation", 120),
		new ListGridField("interface", "Interface", 120),
		new ListGridField("dateCreated", "Date Created", 120)
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}

}
