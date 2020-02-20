package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class UiMenuBarButtonDefinition<E, T> extends ImageUiCommandButtonDefinition<E, T> {

    // Indicated whether this action has a title (and have to be shifted)
    private boolean subTitledAction;

    private boolean asTitle;

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title,
            List<ActionButtonDefinition<E, T>> subActions,
            boolean subTitledAction,
            boolean asTitle) {
        super(eventBus,
                title,
                IconType.ARROW_DOWN,
                true,
                true,
                subActions);
        this.subTitledAction = subTitledAction;
        this.asTitle = asTitle;
    }

    public UiMenuBarButtonDefinition(EventBus eventBus,
            String title, List<ActionButtonDefinition<E, T>> subActions) {
        this(eventBus, title, subActions, false, false);
    }

    @Override
    public boolean isAccessible(E mainEntity, List<T> selectedItems) {
        return true;
    }

    @Override
    public void onClick(E mainEntity, List<T> selectedItems) {
        // Do nothing
    }

    @Override
    public boolean isEnabled(E mainEntity, List<T> selectedItems) {
        for (ActionButtonDefinition<E, T> subAction : getSubActions()) {
            if (subAction.isEnabled(mainEntity, selectedItems) && subAction.isVisible(mainEntity, selectedItems)) {
                return true;
            }
        }

        return false;
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
