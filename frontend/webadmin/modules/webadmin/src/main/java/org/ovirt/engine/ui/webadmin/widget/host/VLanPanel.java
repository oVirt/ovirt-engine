package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.TogglePanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
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

    public static final String CHECK_BOX_COLUMN_WIDTH = "200px"; //$NON-NLS-1$
    public static final String NETWORK_NAME_COLUMN_WIDTH = "200px"; //$NON-NLS-1$
    public static final String ADDRESS_COLUMN_WIDTH = "120px"; //$NON-NLS-1$

    private final boolean isSelectionAvailable;

    public VLanPanel(boolean isSelectionEnabled) {
        super();
        this.isSelectionAvailable = isSelectionEnabled;
    }

    public void addVLans(HostInterfaceLineModel lineModel) {
        boolean hasVlan = lineModel.getVlanSize() != 0;
        for (HostVLan hostVLan : lineModel.getVLans()) {
            add(new VLanElementPanel(hostVLan, isSelectionAvailable));
        }

        if (!hasVlan || !StringHelper.isNullOrEmpty(lineModel.getNetworkName())
                || !StringHelper.isNullOrEmpty(lineModel.getAddress())) {
            add(new VLanElementPanel(lineModel));
        }
    }
}

class VLanElementPanel extends TogglePanel {

    private boolean isSelectionAvailable = false;

    public VLanElementPanel(HostVLan hostVLan, boolean isSelectionEnabled) {
        super(hostVLan);
        this.isSelectionAvailable = isSelectionEnabled;
        add(createRow(hostVLan));
    }

    public VLanElementPanel(HostInterfaceLineModel lineModel) {
        super(lineModel);
        add(createBlankRow(lineModel));
    }

    Grid createRow(final HostVLan hostVLan) {
        Grid row = new Grid(1, 3);
        row.getColumnFormatter().setWidth(0, VLanPanel.CHECK_BOX_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(1, VLanPanel.NETWORK_NAME_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(2, VLanPanel.ADDRESS_COLUMN_WIDTH);

        row.setWidth("100%"); //$NON-NLS-1$
        row.setHeight("100%"); //$NON-NLS-1$

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white"); //$NON-NLS-1$
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        // Check box, icon and name
        HorizontalPanel chekboxPanel = new HorizontalPanel();
        chekboxPanel.setWidth("100%"); //$NON-NLS-1$

        if (isSelectionAvailable) {
            chekboxPanel.add(getCheckBox());
        }
        chekboxPanel.add(new Image(ClientGinjectorProvider.getApplicationResources().splitRotateImage()));
        chekboxPanel.add(new Label(new HostVLanNameRenderer().render(hostVLan)));

        row.setWidget(0, 0, chekboxPanel);

        // Network name
        Label networkName = new Label(hostVLan.getNetworkName());

        if (hostVLan.getInterface().getIsManagement()) {
            networkName.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            networkName.setText("* " + hostVLan.getNetworkName()); //$NON-NLS-1$
        }

        row.setWidget(0, 1, networkName);

        // Address
        row.setWidget(0, 2, new Label(hostVLan.getAddress()));

        return row;
    }

    Grid createBlankRow(final HostInterfaceLineModel lineModel) {
        Grid row = new Grid(1, 3);
        row.getColumnFormatter().setWidth(0, VLanPanel.CHECK_BOX_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(1, VLanPanel.NETWORK_NAME_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(2, VLanPanel.ADDRESS_COLUMN_WIDTH);

        row.setWidth("100%"); //$NON-NLS-1$
        row.setHeight("100%"); //$NON-NLS-1$

        // Empty name
        row.setWidget(0, 0, new Label());

        // Network name
        Label networkName = new Label(lineModel.getNetworkName());

        if (lineModel.getIsManagement()) {
            networkName.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            networkName.setText("* " + lineModel.getNetworkName()); //$NON-NLS-1$
        }

        row.setWidget(0, 1, networkName);

        // Address
        row.setWidget(0, 2, new Label(lineModel.getAddress()));

        return row;
    }

}
