package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
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

        SafeStylesBuilder builder = new SafeStylesBuilder();
        builder.backgroundImage(resources.arrowLeft().getSafeUri());
        builder.height(resources.arrowLeft().getHeight(), Unit.PX);
        builder.width(resources.arrowLeft().getWidth(), Unit.PX);
        HTML leftImageHtml = new HTML(templates.image(builder.toSafeStyles()));
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

            builder = new SafeStylesBuilder();
            builder.backgroundImage(rightImage.getSafeUri());
            builder.height(rightImage.getHeight(), Unit.PX);
            builder.width(rightImage.getWidth(), Unit.PX);
            HTML rightImageHtml = new HTML(templates.image(builder.toSafeStyles()));
            setWidget(row, column, rightImageHtml);
        }
    }
}
