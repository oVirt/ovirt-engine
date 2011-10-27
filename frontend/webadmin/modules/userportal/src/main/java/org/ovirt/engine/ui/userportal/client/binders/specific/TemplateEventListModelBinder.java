package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelBinder;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class TemplateEventListModelBinder implements ListModelBinder {

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

		ArrayList<AuditLog> logs = (ArrayList<AuditLog>)model.getItems();
		
		if (logs != null) {
			for (AuditLog auditLog : logs) {
				ListGridRecord r = new ListGridRecord();
				r.setAttribute("severity", auditLog.getseverity().name());
				r.setAttribute("time", UserPortalTools.formatDateFull(auditLog.getlog_time()));
				r.setAttribute("message", auditLog.getmessage() == null ? "" : SafeHtmlUtils.fromString(auditLog.getmessage()).asString());
				records.add(r);
			}
		}
		
		return records;
	}

	private static ListGridField[] fields = {
		new ListGridField("severity", " ", 30) {{ 
			setType(ListGridFieldType.IMAGE);
			setImageURLPrefix("severity/");
			setImageURLSuffix(".gif");
			setImageHeight(12);
			setImageWidth(14);
			setAlign(Alignment.CENTER);
		}},
		new ListGridField("time", "Time", 120),
		new ListGridField("message", "Message")
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}
}
