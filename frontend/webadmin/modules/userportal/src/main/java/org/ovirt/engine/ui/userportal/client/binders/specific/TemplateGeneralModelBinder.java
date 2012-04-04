package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.EntityModelBinder;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public class TemplateGeneralModelBinder implements EntityModelBinder {

	TemplateGeneralModel model;

	RendererType rendererType = RendererType.DetailViewer;
	
	@Override
	public RecordList calcRecords() {
		
		Record r = new Record();
		VmTemplate template = (VmTemplate)model.getEntity();
		
		r.setAttribute("name", template.getname());
		r.setAttribute("description", template.getdescription() == null ? "" : SafeHtmlUtils.fromString(UserPortalTools.getShortString(template.getdescription(), 50)).asString());
		r.setAttribute("hostCluster", template.getvds_group_name());
		r.setAttribute("os", UserPortalTools.getTranslatedEnum(template.getos()));
		r.setAttribute("defaultDisplayType", UserPortalTools.getTranslatedEnum(template.getdefault_display_type()));		
		r.setAttribute("definedMemory", UserPortalTools.getSizeString(template.getmem_size_mb()));
		r.setAttribute("cpuCores", model.getCpuInfo());
		r.setAttribute("monitors", template.getnum_of_monitors());		
		r.setAttribute("allowConsoleReconnect", template.getAllowConsoleReconnect());		
		r.setAttribute("usbPolicy", template.getusb_policy());			
		r.setAttribute("highlyAvailable", template.getauto_startup());
		r.setAttribute("origin", UserPortalTools.getTranslatedEnum(template.getorigin()));
		
		RecordList records = new RecordList();
		records.add(r);	
		return records;
	}

	@Override
	public void setModel(Model model) {
		this.model = (TemplateGeneralModel)model;
	}

	@Override
	public DetailViewerField[] getFields() {
		return fields;
	}
	
	@Override
	public Integer[] getNumOfRowsInColumn() {
		return new Integer[] {5, 6};
	}

	private static DetailViewerField[] fields = {
		new DetailViewerField("name", "Name"),
		new DetailViewerField("description", "Description"),
		new DetailViewerField("hostCluster", "Host Cluster"),
		new DetailViewerField("os",	"Operating System"),
		new DetailViewerField("defaultDisplayType",	"Default Display Type"),
		new DetailViewerField("definedMemory", "Defined Memory"),
		new DetailViewerField("cpuCores", "Number of CPU Cores"),		
		new DetailViewerField("numOfMonitors", "Number of Monitors"),		
		new DetailViewerField("highlyAvailable", "Highly Available"),
		new DetailViewerField("monitors", "Number of Monitors"),
		new DetailViewerField("origin","Origin")
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}
}
