package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.ui.common.widget.TogglePanel;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.SumUpRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.label.LabelWithToolTip;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
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
        Grid row = new Grid(1, 8);
        row.setHeight("100%"); //$NON-NLS-1$
        row.setWidth("100%"); //$NON-NLS-1$

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white"); //$NON-NLS-1$
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        row.getColumnFormatter().setWidth(0, "10%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(1, "18%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(2, "18%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(3, "18%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(4, "10%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(5, "9%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(6, "9%"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(7, "10%"); //$NON-NLS-1$

        // Check box and interface status icon
        row.setWidget(0, 0, new FlowPanel() {
            {
                if (isSelectionAvailable){
                    add(getCheckBox());
                }

                add(new InterfaceStatusImage(hostInterface.getStatus()));
            }
        });

        // Name
        row.setWidget(0, 1, new Label(hostInterface.getName()));

        // Address
        row.setWidget(0, 2, new Label(hostInterface.getAddress()));

        // MAC
        LabelWithToolTip macLabel = new LabelWithToolTip(hostInterface.getMAC(), 17);
        row.setWidget(0, 3, macLabel);

        // Speed
        row.setWidget(0, 4, new Label() {
            {
                if (hostInterface.getSpeed() != null) {
                    setText(String.valueOf(hostInterface.getSpeed()));
                } else {
                    setText(ClientGinjectorProvider.getApplicationConstants().unAvailablePropertyLabel());
                }
            }
        });

        // Rx rate
        row.setWidget(0, 5, new Label() {
            {
                setText(new RxTxRateRenderer().render(new Double[] {
                        hostInterface.getRxRate(),
                        hostInterface.getSpeed() != null ? hostInterface.getSpeed().doubleValue() : null
                }));
            }
        });

        // Tx rate
        row.setWidget(0, 6, new Label() {
            {
                setText(new RxTxRateRenderer().render(new Double[] {
                        hostInterface.getTxRate(),
                        hostInterface.getSpeed() != null ? hostInterface.getSpeed().doubleValue() : null
                }));
            }
        });

        // Drops
        row.setWidget(0, 7, new Label() {
            {
                setText(new SumUpRenderer().render(new Double[] {
                        hostInterface.getRxDrop(),
                        hostInterface.getTxDrop()
                }));
            }
        });

        return row;
    }

}
