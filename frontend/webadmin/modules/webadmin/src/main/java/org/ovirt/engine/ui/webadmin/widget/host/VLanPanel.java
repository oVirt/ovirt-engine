package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.TogglePanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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
import com.google.gwt.user.client.ui.Widget;


public class VLanPanel extends VerticalPanel {

    public static final String CHECK_BOX_COLUMN_WIDTH = "200px"; //$NON-NLS-1$
    public static final String NETWORK_NAME_COLUMN_WIDTH = "200px"; //$NON-NLS-1$
    public static final String ADDRESS_COLUMN_WIDTH = "120px"; //$NON-NLS-1$
    public static final String OUT_OF_SYNC_WIDTH = "75px"; //$NON-NLS-1$

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

    private final static ApplicationResources resources = AssetProvider.getResources();

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
        // Check box, icon and name
        HorizontalPanel checkboxPanel = new HorizontalPanel();
        checkboxPanel.setWidth("100%"); //$NON-NLS-1$

        if (isSelectionAvailable) {
            checkboxPanel.add(getCheckBox());
        }
        checkboxPanel.add(new Image(resources.splitRotateImage()));
        checkboxPanel.add(new Label(new HostVLanNameRenderer().render(hostVLan)));

        Grid row = createBaseVlanRow(checkboxPanel,
                hostVLan.getInterface().getIsManagement(),
                hostVLan.getNetworkName(),
                hostVLan.getInterface().getNetworkImplementationDetails(),
                hostVLan.getAddress());

        Style gridStyle = row.getElement().getStyle();
        gridStyle.setBorderColor("white"); //$NON-NLS-1$
        gridStyle.setBorderWidth(1, Unit.PX);
        gridStyle.setBorderStyle(BorderStyle.SOLID);

        return row;
    }

    Grid createBlankRow(final HostInterfaceLineModel lineModel) {
        VdsNetworkInterface iface =
                lineModel.getIsBonded() ? lineModel.getInterface() : lineModel.getInterfaces().get(0).getInterface();
        return createBaseVlanRow(new Label(),
                lineModel.getIsManagement(),
                lineModel.getNetworkName(),
                iface.getNetworkImplementationDetails(),
                lineModel.getAddress());
    }

    private Grid createBaseVlanRow(Widget checkBoxWidget,
            boolean networkManagementFlag,
            String networkName,
            final NetworkImplementationDetails networkImplementationDetails,
            String address) {
        Grid row = new Grid(1, 4);
        row.getColumnFormatter().setWidth(0, VLanPanel.CHECK_BOX_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(1, VLanPanel.OUT_OF_SYNC_WIDTH);
        row.getColumnFormatter().setWidth(2, VLanPanel.NETWORK_NAME_COLUMN_WIDTH);
        row.getColumnFormatter().setWidth(3, VLanPanel.ADDRESS_COLUMN_WIDTH);
        row.setWidth("100%"); //$NON-NLS-1$
        row.setHeight("100%"); //$NON-NLS-1$
        row.setWidget(0, 0, checkBoxWidget);
        Label networkNameLabel = new Label(networkName);
        if (networkManagementFlag) {
            networkNameLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            networkNameLabel.setText("* " + networkName); //$NON-NLS-1$
        }

        row.setWidget(0, 1, createSyncPanel(networkImplementationDetails));
        row.setWidget(0, 2, networkNameLabel);
        row.setWidget(0, 3, new Label(address));
        return row;
    }

    private HorizontalPanel createSyncPanel(final NetworkImplementationDetails networkImplementationDetails) {
        HorizontalPanel output = new HorizontalPanel();
        boolean sync = networkImplementationDetails == null ? true : networkImplementationDetails.isInSync();
        if (!sync) {
            output.add(new Image(resources.networkNotSyncImage()));
        }

        return output;
    }

}
