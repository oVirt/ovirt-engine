package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.ui.common.widget.TogglePanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InterfacePanel extends VerticalPanel {

    private final boolean isSelectionAvailable;

    public InterfacePanel(boolean isSelectionEnabled) {
        super();
        this.isSelectionAvailable = isSelectionEnabled;
    }

    public void addInterfaces(List<HostInterface> interfaces) {
        for (HostInterface hostInterface : interfaces) {
            add(new InterfaceElementPanel(hostInterface, isSelectionAvailable));
        }
    }
}

class InterfaceElementPanel extends TogglePanel {

    private final boolean isSelectionAvailable;

    public InterfaceElementPanel(HostInterface hostInterface, boolean isSelectionEnabled) {
        super(hostInterface);
        this.isSelectionAvailable = isSelectionEnabled;
        add(createRow(hostInterface));
    }

    Grid createRow(final HostInterface hostInterface) {
        Grid row = new Grid(1, 2);
        row.setHeight("100%"); //$NON-NLS-1$
        row.setWidth("100%"); //$NON-NLS-1$

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white"); //$NON-NLS-1$
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        row.getColumnFormatter().setWidth(0, "30px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(1, "200px"); //$NON-NLS-1$

        // Check box and interface status icon
        row.setWidget(0, 0, new HorizontalPanel() {
            {
                if (isSelectionAvailable) {
                    add(getCheckBox());
                }

                add(new InterfaceStatusImage(hostInterface.getStatus()));
            }
        });

        // Name
        row.setWidget(0, 1, new InterfaceLabelWithToolTip(hostInterface.getInterface()));

        return row;
    }

}
