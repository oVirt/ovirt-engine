package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class HostInterfaceForm extends Composite {

    private final Grid grid;

    @SuppressWarnings("unchecked")
    public HostInterfaceForm(HostInterfaceListModel listModel) {
        grid = new Grid(1, 3);
        grid.getColumnFormatter().setWidth(0, "65%"); //$NON-NLS-1$
        grid.getColumnFormatter().setWidth(1, "11%"); //$NON-NLS-1$
        grid.getColumnFormatter().setWidth(2, "24%"); //$NON-NLS-1$
        grid.setWidth("100%"); //$NON-NLS-1$
        grid.setHeight("100%"); //$NON-NLS-1$
        initWidget(grid);

        List<HostInterfaceLineModel> interfaceLineModels = (List<HostInterfaceLineModel>) listModel.getItems();
        if (interfaceLineModels != null) {
            showModels(interfaceLineModels);
        }

        listModel.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostInterfaceListModel model = (HostInterfaceListModel) sender;
                List<HostInterfaceLineModel> interfaceLineModels = (List<HostInterfaceLineModel>) model.getItems();
                showModels(interfaceLineModels);
            }
        });
    }

    InterfacePanel createInterfacePanel(HostInterfaceLineModel lineModel) {
        InterfacePanel panel = new InterfacePanel();
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        panel.addInterfaces(lineModel.getInterfaces());
        addClickHandlerToPanel(panel, lineModel);
        return panel;
    }

    BondPanel createBondPanel(HostInterfaceLineModel lineModel) {
        BondPanel panel = new BondPanel(lineModel);
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        addClickHandlerToPanel(panel, lineModel);
        return panel;
    }

    VLanPanel createVLanPanel(HostInterfaceLineModel lineModel) {
        VLanPanel panel = new VLanPanel();
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        panel.addVLans(lineModel);
        addClickHandlerToPanel(panel, lineModel);
        return panel;
    }

    private void addClickHandlerToPanel(Panel panel, final HostInterfaceLineModel lineModel) {
        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                lineModel.setIsSelected(!lineModel.getIsSelected());
            }
        }, ClickEvent.getType());
    }

    void showModels(List<HostInterfaceLineModel> interfaceLineModels) {
        grid.resizeRows(interfaceLineModels.size());
        int row = 0;

        for (HostInterfaceLineModel lineModel : interfaceLineModels) {
            setGridWidget(row, 0, createInterfacePanel(lineModel));
            setGridWidget(row, 1, createBondPanel(lineModel));
            setGridWidget(row, 2, createVLanPanel(lineModel));
            row++;
        }
    }

    void setGridWidget(int row, int col, Widget widget) {
        grid.setWidget(row, col, widget);
        grid.getCellFormatter().setHeight(row, col, "100%"); //$NON-NLS-1$
    }

}
