package org.ovirt.engine.ui.uicommonweb;

/**
 * A default implementation of {@link ICommandTarget}
 */
public abstract class BaseCommandTarget implements ICommandTarget {

    @Override
    public abstract void executeCommand(UICommand uiCommand);

    /*
     * (non-Javadoc) This implementation discards the parameters
     *
     * @see org.ovirt.engine.ui.uicommonweb.ICommandTarget#ExecuteCommand(org.ovirt.engine.ui.uicommonweb.UICommand,
     * java.lang.Object[])
     */
    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }

}
