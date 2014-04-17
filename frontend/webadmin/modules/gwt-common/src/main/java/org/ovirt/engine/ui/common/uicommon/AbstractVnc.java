package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

public class AbstractVnc {

    private String vncHost;
    private String vncPort;
    private String ticket;
    private String title;
    private boolean remapCtrlAltDelete;

    private final ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
    private int ticketValiditySeconds;
    private String toggleFullscreenHotKey;
    private String releaseCursorHotKey;

    public AbstractVnc() {
        setRemapCtrlAltDelete(true);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVncHost() {
        return vncHost;
    }

    public String getVncPort() {
        return vncPort;
    }

    public String getTicket() {
        return ticket;
    }

    public void setVncHost(String vncHost) {
        this.vncHost = vncHost;
    }

    public void setVncPort(String vncPort) {
        this.vncPort = vncPort;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public boolean isRemapCtrlAltDelete() {
        return remapCtrlAltDelete;
    }

    public void setRemapCtrlAltDelete(boolean remapCtrlAltDelete) {
        this.remapCtrlAltDelete = remapCtrlAltDelete;
    }

    protected String getSecureAttentionMapping() {
        return consoleUtils.getRemapCtrlAltDelHotkey();
    }

    public int getTicketValiditySeconds() {
        return ticketValiditySeconds;
    }

    public void setTicketValiditySeconds(int ticketValiditySeconds) {
        this.ticketValiditySeconds = ticketValiditySeconds;
    }

    public String getToggleFullscreenHotKey() {
        return toggleFullscreenHotKey;
    }

    public void setToggleFullscreenHotKey(String toggleFullscreenHotKey) {
        this.toggleFullscreenHotKey = toggleFullscreenHotKey;
    }

    public String getReleaseCursorHotKey() {
        return releaseCursorHotKey;
    }

    public void setReleaseCursorHotKey(String releaseCursorHotKey) {
        this.releaseCursorHotKey = releaseCursorHotKey;
    }

}
