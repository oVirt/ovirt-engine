package org.ovirt.engine.ui.uicommonweb;

/**
 * Defines a Command Target
 */
@SuppressWarnings("unused")
public interface ICommandTarget {
    /**
     * Execute the command with no parameters
     */
    void executeCommand(UICommand command);

    /**
     * Execute the command with the specified parameters
     */
    void executeCommand(UICommand uiCommand, Object... parameters);
}
