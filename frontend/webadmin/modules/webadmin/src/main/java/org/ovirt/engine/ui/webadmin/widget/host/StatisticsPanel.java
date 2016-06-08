package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.ovirt.engine.ui.common.widget.label.LabelWithTextTruncation;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.SumUpRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberLabel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StatisticsPanel extends VerticalPanel {

    public void addInterfaces(List<HostInterface> interfaces) {
        for (HostInterface hostInterface : interfaces) {
            add(new StatisticsElementPanel(hostInterface));
        }
    }
}

class StatisticsElementPanel extends HostInterfaceHorizontalPanel {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final NullableNumberLabel<Long> rxTotalLabel;
    private final NullableNumberLabel<Long> txTotalLabel;

    public StatisticsElementPanel(HostInterface hostInterface) {
        super();
        rxTotalLabel = new NullableNumberLabel<>();
        txTotalLabel = new NullableNumberLabel<>();
        add(createRow(hostInterface));
    }

    Grid createRow(final HostInterface hostInterface) {
        Grid row = new Grid(1, 7);
        row.setHeight("100%"); //$NON-NLS-1$
        row.setWidth("100%"); //$NON-NLS-1$

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white"); //$NON-NLS-1$
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        row.getColumnFormatter().setWidth(0, "120px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(1, "100px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(2, "100px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(3, "100px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(4, "150px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(5, "150px"); //$NON-NLS-1$
        row.getColumnFormatter().setWidth(6, "100px"); //$NON-NLS-1$

        // MAC
        LabelWithTextTruncation macLabel = new LabelWithTextTruncation(hostInterface.getMAC());
        macLabel.setWidth("120px"); //$NON-NLS-1$
        row.setWidget(0, 0, macLabel);

        // Speed
        row.setWidget(0, 1, new Label() {
            {
                if (hostInterface.getSpeed() != null) {
                    setText(String.valueOf(hostInterface.getSpeed()));
                } else {
                    setText(constants.unAvailablePropertyLabel());
                }
            }
        });

        // Rx rate
        row.setWidget(0, 2, new Label() {
            {
                setText(new RxTxRateRenderer().render(new Double[] {
                        hostInterface.getRxRate(),
                        hostInterface.getSpeed() != null ? hostInterface.getSpeed().doubleValue() : null
                }));
            }
        });

        // Tx rate
        row.setWidget(0, 3, new Label() {
            {
                setText(new RxTxRateRenderer().render(new Double[] {
                        hostInterface.getTxRate(),
                        hostInterface.getSpeed() != null ? hostInterface.getSpeed().doubleValue() : null
                }));
            }
        });

        // Rx/Tx totals
        rxTotalLabel.setValue(hostInterface.getRxTotal());
        txTotalLabel.setValue(hostInterface.getTxTotal());
        row.setWidget(0, 4, rxTotalLabel);
        row.setWidget(0, 5, txTotalLabel);

        // Drops
        row.setWidget(0, 6, new Label() {
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
