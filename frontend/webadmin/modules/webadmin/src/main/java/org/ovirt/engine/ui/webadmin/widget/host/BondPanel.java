package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class BondPanel extends HorizontalPanel {

    private static final ApplicationResources resources = AssetProvider.getResources();

    public BondPanel(HostInterfaceLineModel lineModel) {
        super();

        clear();

        Style style = getElement().getStyle();
        style.setBorderColor("white"); //$NON-NLS-1$
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);

        if (lineModel.getIsBonded()) {
            // Bond icon
            Image bondIcon;
            if (InterfaceStatus.UP.equals(lineModel.getInterface().getStatistics().getStatus())) {
                bondIcon = new Image(resources.splitUpImage());
            } else {
                bondIcon = new Image(resources.splitDownImage());
            }

            add(bondIcon);
            setCellWidth(bondIcon, "20%"); //$NON-NLS-1$

            // Bond name
            add(new InterfaceLabel(lineModel.getInterface()));
        } else {
            add(new Label("")); //$NON-NLS-1$
        }
    }

}
