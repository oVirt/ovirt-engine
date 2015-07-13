package org.ovirt.engine.ui.uicommonweb;

/**
 * Defines a Command Target
 */
@SuppressWarnings("unused")
public interface ICommandTarget {
    /**
     * Execute the command with no parameters
     *
     * @param command
     */
    void executeCommand(UICommand command);

    /**
     * Execute the command with the specified parameters
     *
     * @param uiCommand
     * @param parameters
     */
    void executeCommand(UICommand uiCommand, Object... parameters);
}
