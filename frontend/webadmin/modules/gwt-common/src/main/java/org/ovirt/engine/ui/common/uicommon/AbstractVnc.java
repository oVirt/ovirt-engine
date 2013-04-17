package org.ovirt.engine.ui.common.uicommon;

public class AbstractVnc {

    private String vncHost;
    private String vncPort;
    private String ticket;
    private String title;

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

}
