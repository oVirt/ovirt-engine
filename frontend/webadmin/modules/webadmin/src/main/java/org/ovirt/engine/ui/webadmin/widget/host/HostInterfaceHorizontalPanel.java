package org.ovirt.engine.ui.webadmin.widget.host;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class HostInterfaceHorizontalPanel extends HorizontalPanel {

    public HostInterfaceHorizontalPanel() {
        super();
        getElement().getStyle().setBackgroundColor("#F3F7FB"); //$NON-NLS-1$
        getElement().getStyle().setWidth(100, Unit.PCT);
        getElement().getStyle().setHeight(100, Unit.PCT);
    }
}
