package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class NicPanel extends NetworkItemPanel {

    public NicPanel(NetworkInterfaceModel item, NetworkPanelsStyle style) {
        this(item, style, true);
    }

    public NicPanel(NetworkInterfaceModel item, NetworkPanelsStyle style, boolean draggable) {
        super(item, style, draggable);
        getElement().addClassName(style.nicPanel());
        this.actionButton.setStyleName(style.actionButtonNetwork());
        this.actionButton.addStyleName("nicp_actionButton_pfly_fix"); //$NON-NLS-1$
    }

    @Override
    protected Widget getContents() {
        Grid rowPanel = new Grid(1, 5);
        rowPanel.addStyleName("ts3"); //$NON-NLS-1$
        rowPanel.setWidth("100%"); //$NON-NLS-1$
        rowPanel.setHeight("100%"); //$NON-NLS-1$

        ColumnFormatter columnFormatter = rowPanel.getColumnFormatter();
        columnFormatter.setWidth(0, "5px"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "10px"); //$NON-NLS-1$
        columnFormatter.setWidth(2, "30px"); //$NON-NLS-1$
        columnFormatter.setWidth(3, "100%"); //$NON-NLS-1$
        columnFormatter.setWidth(4, "30px"); //$NON-NLS-1$

        Label titleLabel = new Label(item.getName());
        titleLabel.setHeight("100%"); //$NON-NLS-1$
        Image nicImage = new Image(resources.nicIcon());

        rowPanel.setWidget(0, 0, dragImage);
        ImageResource statusImage = getStatusImage();
        if (statusImage != null) {
            rowPanel.setWidget(0, 1, new Image(statusImage));
        }
        rowPanel.setWidget(0, 2, nicImage);
        rowPanel.setWidget(0, 3, titleLabel);
        rowPanel.setWidget(0, 4, actionButton);
        return rowPanel;
    }

    private ImageResource getStatusImage() {
        switch (((NetworkInterfaceModel) item).getStatus()) {
        case UP:
            return resources.nicUp();
        case DOWN:
            return resources.nicDown();
        case NONE:
            return resources.questionMarkImage();
        default:
            return resources.questionMarkImage();
        }
    }

    @Override
    protected void onAction() {
       item.edit();
    }
}
