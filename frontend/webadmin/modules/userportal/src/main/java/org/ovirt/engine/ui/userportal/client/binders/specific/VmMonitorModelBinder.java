package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.EntityModelBinder;
import org.ovirt.engine.ui.userportal.client.components.UserPortalTimerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public class VmMonitorModelBinder implements EntityModelBinder {
	VmMonitorModel model;

	RendererType rendererType = RendererType.MonitorBarsViewer;
	
	private static final int AUTO_REFRESH_INTERVAL = 1000;
	
	@Override
	public RecordList calcRecords() {
		Record r = new Record();
		
		r.setAttribute("cpuUsage", model.getCpuUsage());
		r.setAttribute("memoryUsage", model.getMemoryUsage());
		r.setAttribute("networkUsage", model.getNetworkUsage());
		
		RecordList records = new RecordList();
		records.add(r);	
		return records;
	}

	@Override
	public void setModel(Model model) {
		this.model = (VmMonitorModel)model;
	}

	@Override
	public DetailViewerField[] getFields() {
		return null;
	}
	
	@Override
	public Integer[] getNumOfRowsInColumn() {
		return null;
	}

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}
	
	public void startRefreshTimer() {
		GWT.log("Starting monitor refresh timer");
		monitorRefreshTimer.run();
	}
	
	public void cancelRefreshTimer() {
		GWT.log("Stopping monitor refresh timer");
		monitorRefreshTimer.cancel();
	}
	
	
	private Timer monitorRefreshTimer = UserPortalTimerFactory.factoryTimer("VmMonitorRefreshTimer", new Timer() {
		@Override
		public void run() {
			model.getRefreshCommand().Execute();
			this.schedule(AUTO_REFRESH_INTERVAL);
		}
	});
}
