package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;

import com.google.gwt.text.shared.AbstractRenderer;

public class HostInterfaceBondNameRenderer extends AbstractRenderer<HostInterfaceLineModel> {

    @Override
    public String render(HostInterfaceLineModel object) {
        StringBuilder sb = new StringBuilder(object.getBondName());

        if (object.getAddress() != null && !object.getAddress().isEmpty()) {
            sb.append(" (").append(object.getAddress()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return sb.toString();
    }

}
