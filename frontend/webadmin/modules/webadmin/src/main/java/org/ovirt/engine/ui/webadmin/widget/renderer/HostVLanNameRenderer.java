package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;

import com.google.gwt.text.shared.AbstractRenderer;

public class HostVLanNameRenderer extends AbstractRenderer<HostVLan> {

    @Override
    public String render(HostVLan object) {
        StringBuilder sb = new StringBuilder(object.getName());

        if (object.getInterface().getVlanId() != null) {
            sb.append(" (").append(object.getInterface().getVlanId()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return sb.toString();
    }

}
