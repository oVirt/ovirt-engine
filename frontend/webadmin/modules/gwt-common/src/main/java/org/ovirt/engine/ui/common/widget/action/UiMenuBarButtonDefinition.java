package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class UiMenuBarButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    private final List<ActionButtonDefinition<T>> subActions;

    // Indicated whether this action has a title (and have to be shifted)
    private boolean subTitledAction;

    private boolean asTitle;

    /**
     * A ClientBundle that provides images for this widget.
     */
    public interface Resources extends ClientBundle {

        @Source("org/ovirt/engine/ui/webadmin/images/triangle_down.gif")
        @ImageOptions(width = 7, height = 5)
        ImageResource triangle_down();

    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            boolean subTitledAction, CommandLocation commandLocation,
            boolean asTitle, Resources resources) {
        super(eventBus, title, resources.triangle_down(), resources.triangle_down(),
                true, true, commandLocation);
        this.subActions = subActions;
        this.subTitledAction = subTitledAction;
        this.asTitle = asTitle;
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            Resources resources) {
        this(eventBus, title, subActions, false, resources);
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            CommandLocation commandLocation, Resources resources) {
        this(eventBus, title, subActions, false, commandLocation, false, resources);
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            boolean asTitle, Resources resources) {
        this(eventBus, title, subActions, false, CommandLocation.ContextAndToolBar, asTitle, resources);
    }

    @Override
    public boolean isAccessible(List<T> selectedItems) {
        return true;
    }

    @Override
    public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
        return eventBus.addHandler(InitializeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public void onClick(List<T> selectedItems) {
        // Do nothing
    }

    @Override
    public boolean isEnabled(List<T> selectedItems) {
        boolean isEnabled = false;

        for (ActionButtonDefinition<T> subAction : getSubActions()) {
            if (subAction.isEnabled(selectedItems) && subAction.isVisible(selectedItems)) {
                return true;
            }
        }

        return isEnabled;
    }

    @Override
    public void update() {
        // Do nothing
    }

    /**
     * This function returns the sub menu actions
     *
     * @return the sub menu actions
     */
    public List<ActionButtonDefinition<T>> getSubActions() {
        return subActions;
    }

    @Override
    public String getUniqueId() {
        return getText();
    }

    @Override
    public boolean isSubTitledAction() {
        return subTitledAction;
    }

    @Override
    public SafeHtml getMenuItemTooltip() {
        return null;
    }

    @Override
    protected UICommand resolveCommand() {
        return null;
    }

    public boolean isAsTitle() {
        return asTitle;
    }

}
