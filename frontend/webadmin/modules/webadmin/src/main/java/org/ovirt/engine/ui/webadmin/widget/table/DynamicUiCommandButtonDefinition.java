package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.resources.client.ImageResource;

/**
 * A Button Definition for UICommands that must be Updated during each selection
 * 
 * @param <T>
 *            The Command Target
 * @param <M>
 *            The Command Model
 */
public abstract class DynamicUiCommandButtonDefinition<M, T extends ICommandTarget> extends ImageUiCommandButtonDefinition<M> {

    private static final UICommand EMPTY_COMMAND = new UICommand("Empty", null);
    private final T target;

    {
        EMPTY_COMMAND.setIsExecutionAllowed(false);
    }

    /**
     * Create a Button Definition for UICommands that must be Updated during each selection
     * 
     * @param target
     *            The command Target
     * @param title
     *            The Command Text title
     * @param enabledImage
     *            The Image to display when the command is Enabled
     * @param disabledImage
     *            The Image to display when the command is Disabled
     */
    public DynamicUiCommandButtonDefinition(T target,
            String title,
            ImageResource enabledImage,
            ImageResource disabledImage) {
        super(EMPTY_COMMAND, title, enabledImage, disabledImage);
        this.target = target;
    }

    /**
     * Called when the command needs to be updated after selection changes
     * 
     * @param target
     *            The command Target
     */
    public void updateCommand() {
        UICommand updatedCommand = null;
        try {
            updatedCommand = getUpdatedCommand(target);
        } catch (Exception npe) {
            // Ignore possible Exceptions
        }
        setCommand(updatedCommand == null ? EMPTY_COMMAND : updatedCommand);
    }

    /**
     * Provide the updated command
     * 
     * @param target
     *            The command Target
     * @return
     */
    protected abstract UICommand getUpdatedCommand(T target);

}
