package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class UiMenuBarButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    private final List<ActionButtonDefinition<T>> subActions;

    // Indicated whether this action has a title (and have to be shifted)
    private boolean subTitledAction;

    private boolean asTitle;

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            boolean subTitledAction, boolean asTitle) {
        super(eventBus, title, IconType.ARROW_DOWN,
                true, true);
        this.subActions = subActions;
        this.subTitledAction = subTitledAction;
        this.asTitle = asTitle;
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions) {
        this(eventBus, title, subActions, false, false);
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<T>> subActions,
            boolean asTitle) {
        this(eventBus, title, subActions, false, asTitle);
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
