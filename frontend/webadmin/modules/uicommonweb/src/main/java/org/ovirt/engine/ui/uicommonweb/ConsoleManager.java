package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;

public interface ConsoleManager {
/**
     * Takes a model a protocol under which to connect. When successful, returns null. When not successful, returns
     * message describing the problem.
     */
    public String connectToConsole(HasConsoleModel model);

}
