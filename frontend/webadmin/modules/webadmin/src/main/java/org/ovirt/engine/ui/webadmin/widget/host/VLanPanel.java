package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.TogglePanel;
import org.ovirt.engine.ui.webadmin.widget.renderer.HostVLanNameRenderer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VLanPanel extends VerticalPanel {

    public static final String CHECK_BOX_COLUMN_WIDTH = "50%";
    public static final String NETWORK_NAME_COLUMN_WIDTH = "50%";

    public void addVLans(HostInterfaceLineModel lineModel) {
        for (HostVLan hostVLan : lineModel.getVLans()) {
            add(new VLanElementPanel(hostVLan));
        }

        if (lineModel.getVLans().isEmpty()) {
            add(new VLanElementPanel(lineModel));
        }
    }

}

class VLanElementPanel extends TogglePanel {

    public VLanElementPanel(HostVLan hostVLan) {
        super(hostVLan);
        add(createRow(hostVLan));
    }

    public VLanElementPanel(HostInterfaceLineModel lineModel) {
        super(lineModel);
        add(createBlankRow(lineModel));
    }

    Grid createRow(final HostVLan hostVLan) {
        Grid row = new Grid(1, 2);
        row.getColumnFormatter().setWidth(0, VLanPanel.CHECK_BOX_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(1, VLanPanel.NETWORK_NAME_COLUMN_WIDTH);
        row.getCellFormatter().setHeight(0, 0, "100%");
        row.getCellFormatter().setHeight(0, 1, "100%");
        row.setWidth("100%");
        row.setHeight("100%");

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white");
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        // Check box, icon and name
        HorizontalPanel chekboxPanel = new HorizontalPanel();
        chekboxPanel.setWidth("100%");

        chekboxPanel.add(getCheckBox());
        chekboxPanel.add(new Image(ClientGinjectorProvider.instance().getApplicationResources().splitRotateImage()));
        chekboxPanel.add(new Label(new HostVLanNameRenderer().render(hostVLan)));

        row.setWidget(0, 0, chekboxPanel);

        // Network name
        row.setWidget(0, 1, new Label(hostVLan.getNetworkName()));

        return row;
    }

    Grid createBlankRow(final HostInterfaceLineModel lineModel) {
        Grid row = new Grid(1, 2);
        row.getColumnFormatter().setWidth(0, VLanPanel.CHECK_BOX_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(1, VLanPanel.NETWORK_NAME_COLUMN_WIDTH);
        row.getCellFormatter().setHeight(0, 0, "100%");
        row.getCellFormatter().setHeight(0, 1, "100%");
        row.setWidth("100%");
        row.setHeight("100%");

        // Empty name
        row.setWidget(0, 0, new Label());

        // Network name
        Label networkName = new Label(lineModel.getNetworkName());

        if (lineModel.getIsManagement()) {
            networkName.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            networkName.setText("* " + lineModel.getNetworkName());
        }

        row.setWidget(0, 1, networkName);

        return row;
    }

}
