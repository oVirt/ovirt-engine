package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;

public class ConnectorPanel extends FlexTable {

    public ConnectorPanel(NetworkInterfaceModel nicModel, NetworkPanelsStyle style) {
        super();
        int networkSize = nicModel.getItems().size();
        if (networkSize == 0) {
            return;
        }
        ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();

        setCellPadding(0);
        setCellSpacing(0);
        FlexCellFormatter flexCellFormatter = getFlexCellFormatter();
        flexCellFormatter.setRowSpan(0, 0, networkSize);

        setWidget(0, 0, new Image(networkSize > 1 ? resources.arrowLeftMiddle() : resources.arrowLeft()));

        for (int i = 0; i < networkSize; i++) {
            int row = i;
            int column = i > 0 ? 0 : 1;
            ImageResource rightImage;
            if (networkSize == 1) {
                rightImage = resources.arrowRightOne();
            } else {
                if (i == 0) {
                    rightImage = resources.arrowRightTop();
                } else if (i == networkSize - 1) {
                    rightImage = resources.arrowRightBottom();
                } else {
                    rightImage = resources.arrowRightMiddle();
                }
            }
            setWidget(row, column, new Image(rightImage));
        }
        setHeight((50 * networkSize) + "px");
    }
}
