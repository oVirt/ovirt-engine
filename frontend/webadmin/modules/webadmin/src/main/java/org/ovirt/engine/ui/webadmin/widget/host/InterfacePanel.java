package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.ui.common.widget.TogglePanel;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.SumUpRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InterfacePanel extends VerticalPanel {

    public void addInterfaces(List<HostInterface> interfaces) {
        for (HostInterface hostInterface : interfaces) {
            add(new InterfaceElementPanel(hostInterface));
        }
    }

}

class InterfaceElementPanel extends TogglePanel {

    public InterfaceElementPanel(HostInterface hostInterface) {
        super(hostInterface);
        add(createRow(hostInterface));
    }

    Grid createRow(final HostInterface hostInterface) {
        Grid row = new Grid(1, 8);
        row.setHeight("100%");
        row.setWidth("100%");

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white");
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        row.getColumnFormatter().setWidth(0, "10%");
        row.getColumnFormatter().setWidth(1, "18%");
        row.getColumnFormatter().setWidth(2, "18%");
        row.getColumnFormatter().setWidth(3, "18%");
        row.getColumnFormatter().setWidth(4, "10%");
        row.getColumnFormatter().setWidth(5, "9%");
        row.getColumnFormatter().setWidth(6, "9%");
        row.getColumnFormatter().setWidth(7, "10%");

        // Check box and interface status icon
        row.setWidget(0, 0, new FlowPanel() {
            {
                add(getCheckBox());

                add(new InterfaceStatusImage(hostInterface.getStatus(),
                        ClientGinjectorProvider.instance().getApplicationResources()));
            }
        });

        // Name
        row.setWidget(0, 1, new Label(hostInterface.getName()));

        // Address
        row.setWidget(0, 2, new Label(hostInterface.getAddress()));

        // MAC
        row.setWidget(0, 3, new Label(hostInterface.getMAC()));

        // Speed
        row.setWidget(0, 4, new Label() {
            {
                if (hostInterface.getSpeed() != null) {
                    setText(String.valueOf(hostInterface.getSpeed()));
                } else {
                    setText(ClientGinjectorProvider.instance().getApplicationConstants().unAvailablePropertyLabel());
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
