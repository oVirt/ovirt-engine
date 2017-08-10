package org.ovirt.engine.ui.uicommonweb;

/**
 * A default implementation of {@link ICommandTarget}
 */
public abstract class BaseCommandTarget implements ICommandTarget {

    @Override
    public abstract void executeCommand(UICommand uiCommand);

    /**
     * {@inheritDoc}
     * <p>
     * This implementation discards the parameters.
     *
     * @see ICommandTarget#executeCommand(UICommand,Object[])
     */
    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }

}
