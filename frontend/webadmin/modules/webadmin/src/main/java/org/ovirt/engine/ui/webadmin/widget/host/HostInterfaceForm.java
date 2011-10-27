package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;

// TODO list
// - Host NIC subtab modify as form-based
// - use this widget within Host NIC subtab
public class HostInterfaceForm extends Composite {

    private final Grid grid;

    @SuppressWarnings("unchecked")
    public HostInterfaceForm(HostInterfaceListModel listModel) {
        listModel.getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostInterfaceListModel model = (HostInterfaceListModel) sender;
                List<HostInterfaceLineModel> interfaceLineModels = (List<HostInterfaceLineModel>) model.getItems();
                showModels(interfaceLineModels);
            }
        });

        grid = new Grid(1, 3);
        grid.getColumnFormatter().setWidth(0, "65%");
        grid.getColumnFormatter().setWidth(1, "11%");
        grid.getColumnFormatter().setWidth(2, "24%");
        grid.setWidth("100%");
        grid.setHeight("100%");
        initWidget(grid);

        List<HostInterfaceLineModel> interfaceLineModels = (List<HostInterfaceLineModel>) listModel.getItems();
        if (interfaceLineModels != null) {
            showModels(interfaceLineModels);
        }
    }

    InterfacePanel createInterfacePanel(HostInterfaceLineModel lineModel) {
        InterfacePanel panel = new InterfacePanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.addInterfaces(lineModel.getInterfaces());
        return panel;
    }

    BondPanel createBondPanel(HostInterfaceLineModel lineModel) {
        BondPanel panel = new BondPanel(lineModel);
        panel.setWidth("100%");
        panel.setHeight("100%");
        return panel;
    }

    VLanPanel createVLanPanel(HostInterfaceLineModel lineModel) {
        VLanPanel panel = new VLanPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.addVLans(lineModel);
        return panel;
    }

    private void showModels(List<HostInterfaceLineModel> interfaceLineModels) {
        int row = 0;
        grid.resizeRows(interfaceLineModels.size());
        for (HostInterfaceLineModel lineModel : interfaceLineModels) {
            grid.setWidget(row, 0, createInterfacePanel(lineModel));
            grid.getCellFormatter().setHeight(row, 0, "100%");
            grid.setWidget(row, 1, createBondPanel(lineModel));
            grid.getCellFormatter().setHeight(row, 1, "100%");
            grid.setWidget(row, 2, createVLanPanel(lineModel));
            grid.getCellFormatter().setHeight(row, 2, "100%");
            row++;
        }
    }

}
