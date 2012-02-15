package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

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

    public UiMenuBarButtonDefinition(String title,
            List<ActionButtonDefinition<T>> subActions,
            boolean subTitledAction, boolean availableOnlyFromContext,
            boolean asTitle, Resources resources) {
        super(title, resources.triangle_down(), resources.triangle_down(), true, true, availableOnlyFromContext);
        this.subActions = subActions;
        this.subTitledAction = subTitledAction;
        this.asTitle = asTitle;
    }

    public UiMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions, Resources resources) {
        this(title, subActions, false, resources);
    }

    public UiMenuBarButtonDefinition(String title,
            List<ActionButtonDefinition<T>> subActions,
            boolean asTitle, Resources resources) {
        this(title, subActions, false, false, asTitle, resources);
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public void setAccessible(boolean accessible) {
        // Do nothing

    }

    @Override
    public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
        return getEventBus().addHandler(InitializeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getEventBus().fireEvent(event);
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
    public boolean isImplemented() {
        return true;
    }

    @Override
    public boolean isImplInUserPortal() {
        return false;
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
        return getTitle();
    }

    @Override
    public boolean isSubTitledAction() {
        return subTitledAction;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    protected UICommand resolveCommand() {
        return null;
    }

    @Override
    public boolean isVisible(List<T> selectedItems) {
        return true;
    }

    @Override
    protected abstract EventBus getEventBus();

    @Override
    protected abstract CommonApplicationTemplates getCommonApplicationTemplates();

    public boolean isAsTitle() {
        return asTitle;
    }

}
