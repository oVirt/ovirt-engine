package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class ConnectorPanel extends FlexTable {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    public ConnectorPanel(NetworkInterfaceModel nicModel, NetworkPanelsStyle style) {
        super();
        int networkSize = nicModel.getTotalItemSize();
        if (networkSize == 0) {
            return;
        }

        setCellPadding(0);
        setCellSpacing(0);
        FlexCellFormatter flexCellFormatter = getFlexCellFormatter();
        flexCellFormatter.setRowSpan(0, 0, networkSize);

        HTML leftImageHtml = new HTML(templates.image(resources.arrowLeft().getURL(), resources.arrowLeft().getHeight(), resources.arrowLeft().getWidth()));
        setWidget(0, 0, leftImageHtml);

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

            HTML rightImageHtml = new HTML(templates.image(rightImage.getURL(), rightImage.getHeight(), rightImage.getWidth()));
            setWidget(row, column, rightImageHtml);
        }
    }
}
