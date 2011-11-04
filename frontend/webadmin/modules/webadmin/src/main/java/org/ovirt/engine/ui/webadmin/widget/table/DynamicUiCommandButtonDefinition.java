package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.resources.client.ImageResource;

/**
 * {@link UICommand} button definition that gets updated when the selection of the corresponding UiCommon model changes.
 * 
 * @param <T>
 *            Table row data type.
 */
public abstract class DynamicUiCommandButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    /**
     * Creates a new button that gets updated upon each selection change.
     * 
     * @param title
     *            The Command Text title
     * @param enabledImage
     *            The Image to display when the command is Enabled
     * @param disabledImage
     *            The Image to display when the command is Disabled
     */
    public DynamicUiCommandButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        super(title, enabledImage, disabledImage);
    }

    /**
     * Called when the selection of the corresponding UiCommon model changes.
     */
    @Override
    public void updateCommand() {
        super.updateCommand();
    }

}
