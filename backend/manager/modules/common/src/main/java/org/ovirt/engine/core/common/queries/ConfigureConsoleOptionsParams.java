package org.ovirt.engine.core.common.queries;


import org.ovirt.engine.core.common.console.ConsoleOptions;

public class ConfigureConsoleOptionsParams extends ConsoleOptionsParams {

    boolean setTicket = false;

    public ConfigureConsoleOptionsParams() { }

    public ConfigureConsoleOptionsParams(ConsoleOptions options, boolean setTicket) {
        super(options);
        this.setTicket = setTicket;
    }

    public boolean isSetTicket() {
        return setTicket;
    }
}
