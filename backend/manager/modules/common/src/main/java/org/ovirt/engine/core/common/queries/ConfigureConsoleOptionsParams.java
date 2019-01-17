package org.ovirt.engine.core.common.queries;


import org.ovirt.engine.core.common.console.ConsoleOptions;

public class ConfigureConsoleOptionsParams extends ConsoleOptionsParams {

    public static final String VNC_USERNAME_PREFIX = "vnc-";

    boolean setTicket = false;

    private String engineBaseUrl;

    private String consoleClientResourcesUrl;

    public ConfigureConsoleOptionsParams() { }

    public ConfigureConsoleOptionsParams(ConsoleOptions options, boolean setTicket) {
        super(options);
        this.setTicket = setTicket;
    }

    public boolean isSetTicket() {
        return setTicket;
    }

    public String getEngineBaseUrl() {
        return engineBaseUrl;
    }

    public void setEngineBaseUrl(String engineBaseUrl) {
        this.engineBaseUrl = engineBaseUrl;
    }

    public String getConsoleClientResourcesUrl() {
        return consoleClientResourcesUrl;
    }

    public void setConsoleClientResourcesUrl(String consoleClientResourcesUrl) {
        this.consoleClientResourcesUrl = consoleClientResourcesUrl;
    }
}
