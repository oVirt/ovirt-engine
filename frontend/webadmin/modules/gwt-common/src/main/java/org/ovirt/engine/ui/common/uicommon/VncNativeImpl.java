package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVnc;

public class VncNativeImpl extends AbstractVnc implements IVnc {

    @Override
    public void invokeClient() {
        ConsoleOptions options = getOptions();
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]"); //$NON-NLS-1$
        configBuilder.append("\ntype=vnc") //$NON-NLS-1$
                .append("\nhost=").append(options.getHost()) //$NON-NLS-1$
                .append("\nport=").append(options.getPort()) //$NON-NLS-1$
                .append("\npassword=").append(options.getTicket()) //$NON-NLS-1$
                .append("\n# Password is valid for ") //$NON-NLS-1$
                .append(ConsoleOptions.TICKET_VALIDITY_SECONDS).append(" seconds.") //$NON-NLS-1$
                .append("\ndelete-this-file=1") //$NON-NLS-1$
                .append("\ntitle=").append(options.getTitle()); //$NON-NLS-1$

        if (!StringHelper.isNullOrEmpty(options.getToggleFullscreenHotKey())) {
            configBuilder.append("\ntoggle-fullscreen=").append(options.getToggleFullscreenHotKey()); //$NON-NLS-1$
        }

        String releaseCursorHotKey = options.getReleaseCursorHotKey();
        if (!StringHelper.isNullOrEmpty(releaseCursorHotKey)) {
            configBuilder.append("\nrelease-cursor=").append(releaseCursorHotKey); //$NON-NLS-1$
        }

        if (options.isRemapCtrlAltDelete()) {
            configBuilder.append("\nsecure-attention=").append(ConsoleOptions.SECURE_ATTENTION_MAPPING); //$NON-NLS-1$
        }

        ConsoleModel.makeConsoleConfigRequest("console.vv", "application/x-virt-viewer; charset=UTF-8", configBuilder.toString()); //$NON-NLS-1$ $NON-NLS-2$
    }

}
