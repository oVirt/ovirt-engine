package org.ovirt.engine.ui.uicommonweb;

/**
 * Defines a Command Target
 */
@SuppressWarnings("unused")
public interface ICommandTarget
{
    /**
     * Execute the command with no parameters
     *
     * @param command
     */
    void ExecuteCommand(UICommand command);

    /**
     * Execute the command with the specified parameters
     *
     * @param uiCommand
     * @param parameters
     */
    void ExecuteCommand(UICommand uiCommand, Object... parameters);
}
