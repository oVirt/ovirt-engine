package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;

public class AbstractVnc {

    private final ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.VNC);

    public String getTitle() {
        return consoleOptions.getTitle();
    }

    public void setTitle(String title) {
        consoleOptions.setTitle(title);
    }

    public String getVncHost() {
        return consoleOptions.getHost();
    }

    public String getVncPort() {
        Integer port = consoleOptions.getPort();
        return (port == null)
                ? null
                : port.toString();
    }

    public String getTicket() {
        return consoleOptions.getTicket();
    }

    public void setVncHost(String vncHost) {
        consoleOptions.setHost(vncHost);
    }

    public void setVncPort(String vncPort) {
        consoleOptions.setPort((vncPort == null)
                ? null
                : Integer.parseInt(vncPort));
    }

    public void setTicket(String ticket) {
        consoleOptions.setTicket(ticket);
    }

    public boolean isRemapCtrlAltDelete() {
        return consoleOptions.isRemapCtrlAltDelete();
    }

    public void setRemapCtrlAltDelete(boolean remapCtrlAltDelete) {
        consoleOptions.setRemapCtrlAltDelete(remapCtrlAltDelete);
    }

    protected String getSecureAttentionMapping() {
        return ConsoleOptions.SECURE_ATTENTION_MAPPING;
    }

    public String getToggleFullscreenHotKey() {
        return consoleOptions.getToggleFullscreenHotKey();
    }

    public void setToggleFullscreenHotKey(String toggleFullscreenHotKey) {
        consoleOptions.setToggleFullscreenHotKey(toggleFullscreenHotKey);
    }

    public String getReleaseCursorHotKey() {
        return consoleOptions.getReleaseCursorHotKey();
    }

    public void setReleaseCursorHotKey(String releaseCursorHotKey) {
        consoleOptions.setReleaseCursorHotKey(releaseCursorHotKey);
    }

}
