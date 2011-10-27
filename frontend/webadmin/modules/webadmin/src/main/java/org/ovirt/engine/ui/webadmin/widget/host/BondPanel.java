package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.TogglePanel;
import org.ovirt.engine.ui.webadmin.widget.renderer.HostInterfaceBondNameRenderer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class BondPanel extends TogglePanel {

    public BondPanel(HostInterfaceLineModel lineModel) {
        super(lineModel);
        clear();
        Style style = getElement().getStyle();
        style.setBorderColor("white");
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);

        if (lineModel.getIsBonded()) {
            add(getCheckBox());

            // Bond icon
            add(new Image(ClientGinjectorProvider.instance().getApplicationResources().splitImage()));

            // Bond name
            add(new Label(new HostInterfaceBondNameRenderer().render(lineModel)));
        } else {
            add(new Label("    "));
        }
    }

}
