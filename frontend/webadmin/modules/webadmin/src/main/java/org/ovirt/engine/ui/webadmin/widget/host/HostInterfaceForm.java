package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

public class HostInterfaceForm extends Composite {

    private final Grid grid;
    private boolean isSelectionAvailable;

    @SuppressWarnings("unchecked")
    public HostInterfaceForm(final HostInterfaceListModel listModel) {
        isSelectionAvailable = listModel.getIsSelectionAvailable();
        grid = new Grid(1, 3);
        grid.getColumnFormatter().setWidth(0, "1080px"); //$NON-NLS-1$
        grid.getColumnFormatter().setWidth(1, "210px"); //$NON-NLS-1$
        grid.getColumnFormatter().setWidth(2, "420px"); //$NON-NLS-1$
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

        listModel.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("isSelectionAvailable".equals(propName)) { //$NON-NLS-1$
                    isSelectionAvailable = listModel.getIsSelectionAvailable();

                    if (listModel.getItems()!=null){
                        showModels((List<HostInterfaceLineModel>) listModel.getItems());
                    }
                }
            }
        });
    }

    InterfacePanel createInterfacePanel(HostInterfaceLineModel lineModel) {
        InterfacePanel panel = new InterfacePanel(isSelectionAvailable);
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        panel.addInterfaces(lineModel.getInterfaces());
        return panel;
    }

    BondPanel createBondPanel(HostInterfaceLineModel lineModel) {
        BondPanel panel = new BondPanel(lineModel, isSelectionAvailable);
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        return panel;
    }

    VLanPanel createVLanPanel(HostInterfaceLineModel lineModel) {
        VLanPanel panel = new VLanPanel(isSelectionAvailable);
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.setHeight("100%"); //$NON-NLS-1$
        panel.addVLans(lineModel);
        return panel;
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
