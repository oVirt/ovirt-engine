package org.ovirt.engine.ui.userportal.client.binders.interfaces;

import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import com.smartgwt.client.data.RecordList;

public interface ModelToViewerBinder {
	
	public RecordList calcRecords();
	
	public void setModel(Model model);
	
	public RendererType getRendererType();
}
