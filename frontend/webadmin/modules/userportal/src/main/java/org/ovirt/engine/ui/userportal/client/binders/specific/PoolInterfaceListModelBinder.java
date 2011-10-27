package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewNICModalPanel;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class PoolInterfaceListModelBinder implements ListModelBinder {

	ListModel model;

	RendererType rendererType = RendererType.Grid;
	
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

		ArrayList<VmNetworkInterface> interfaces = (ArrayList<VmNetworkInterface>)model.getItems();

		Translator translator = EnumTranslator.Create(VmInterfaceType.class);

		if (interfaces != null) {
			for (VmNetworkInterface intrface : interfaces) {
				ListGridRecord r = new ListGridRecord();
				r.setAttribute("name", intrface.getName());
				r.setAttribute("networkName", intrface.getNetworkName());
				r.setAttribute("type", translator.get(VmInterfaceType.forValue(intrface.getType())));
				r.setAttribute("mac", intrface.getMacAddress());
				r.setAttribute("speed", intrface.getSpeed());
				r.setAttribute("rx", intrface.getStatistics().getReceiveRate());
				r.setAttribute("tx", intrface.getStatistics().getReceiveRate());
				r.setAttribute("drops", (intrface.getStatistics().getReceiveRate() == null || intrface.getStatistics().getReceiveRate() == null) ? "[N/A]" : new Double(intrface.getStatistics().getReceiveRate() + intrface.getStatistics().getReceiveRate()).toString());
				r.setAttribute("entity", intrface);
				r.setAttribute("entityGuid", intrface.getId());
				records.add(r);
			}
		}
		return records;
	}

	private static ListGridField[] fields = {
			new ListGridField("name", "Name"),
			new ListGridField("networkName", "Network Name"),
			new ListGridField("type", "Type", 120),
			new ListGridField("mac", "MAC", 120),
			new ListGridField("speed", "Speed (Mbps)", 110),
			new ListGridField("rx", "Rx (Mbps)", 100),
			new ListGridField("tx", "Tx (Mbps)", 90),
			new ListGridField("drops", "Drops (Pkts)", 90),
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}
	
}
