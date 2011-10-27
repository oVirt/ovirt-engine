package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.EntityModelBinder;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public class PoolGeneralModelBinder implements EntityModelBinder {

	PoolGeneralModel model;

	RendererType rendererType = RendererType.DetailViewer;
	
	@Override
	public RecordList calcRecords() {
		Record r = new Record();
		
		r.setAttribute("name", model.getName());
		r.setAttribute("description", model.getDescription() == null ? "" : SafeHtmlUtils.fromString(UserPortalTools.getShortString(model.getDescription(), 50)).asString());
		r.setAttribute("template", model.getTemplate());
		r.setAttribute("os", model.getOS());
		r.setAttribute("defaultDisplayType", model.getDefaultDisplayType());		
		r.setAttribute("definedMemory", model.getDefinedMemory());
		r.setAttribute("minAllocatedMemory", model.getMinAllocatedMemory());		
		r.setAttribute("cpuCores", model.getCpuInfo());
		r.setAttribute("monitors", model.getMonitorCount());		
		r.setAttribute("usbPolicy", model.getUsbPolicy());			
		r.setAttribute("origin", model.getOrigin());
		r.setAttribute("domain", model.getDomain() == null ? "" : SafeHtmlUtils.fromString(model.getDomain()).asString());
		r.setAttribute("timeZone", model.getTimeZone());
		r.setAttribute("storageDomain", model.getStorageDomain());
		r.setAttribute("defaultHost", model.getDefaultHost());
		
		// Set hidden property for hidable items		
		r.setAttribute("domain_hidden", !model.getHasDomain());
		r.setAttribute("timeZone_hidden", !model.getHasTimeZone());
		r.setAttribute("storageDomain_hidden", !model.getHasStorageDomain());
		
		RecordList records = new RecordList();
		records.add(r);	
		return records;
	}

	@Override
	public void setModel(Model model) {
		this.model = (PoolGeneralModel)model;
	}

	@Override
	public DetailViewerField[] getFields() {
		return fields;
	}
	
	@Override
	public Integer[] getNumOfRowsInColumn() {
		return new Integer[] {5, 5, 4};
	}

	private static DetailViewerField[] fields = {
		new DetailViewerField("name", "Name"),
		new DetailViewerField("description", "Description"),
		new DetailViewerField("template", "Template"),
		new DetailViewerField("os", "Operating System"),
		new DetailViewerField("defaultDisplayType", "Default Display Type"),
		new DetailViewerField("definedMemory", "Defined Memory"),
		new DetailViewerField("minAllocatedMemory", "Physical Memory Guaranteed"),		
		new DetailViewerField("cpuCores", "Number of CPU Cores"),
		new DetailViewerField("monitors", "Number of Monitors"),
		new DetailViewerField("usbPolicy", "USB Policy"),
		new DetailViewerField("domain", "Domain"),
		new DetailViewerField("timeZone", "Time Zone"),
		new DetailViewerField("defaultHost", "Run On")
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}


}
