package org.ovirt.engine.ui.common.widget.action;

import static java.util.Collections.emptyList;
import static org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.MODEL_CHANGE_RELEVANT_FOR_ACTIONS;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Base implementation of {@link ActionButtonDefinition} interface.
 *
 * @param <T> Action panel item type.
 */
public abstract class AbstractButtonDefinition<E, T> implements ActionButtonDefinition<E, T> {

    protected final EventBus eventBus;

    private final List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    private final List<ActionButtonDefinition<E, T>> subActions;

    protected final SafeHtml title;

    // Indicates whether this action button has a title action
    private final boolean subTitledAction;

    private IEventListener<? super PropertyChangedEventArgs> updateOnModelChangeRelevantForActionsListener = (ev, sender, args) -> {
        if (args.propertyName.equals(MODEL_CHANGE_RELEVANT_FOR_ACTIONS)) {
            update();
        }
    };

    public AbstractButtonDefinition(EventBus eventBus,
            String title,
            boolean subTitledAction,
            List<ActionButtonDefinition<E, T>> subActions) {
        this.subActions = new ArrayList<>(subActions);
        this.eventBus = eventBus;
        this.title = SafeHtmlUtils.fromSafeConstant(title);
        this.subTitledAction = subTitledAction;
    }

    public AbstractButtonDefinition(EventBus eventBus,
            String title,
            boolean subTitledAction) {
        this(eventBus, title, subTitledAction, emptyList());
    }

    public AbstractButtonDefinition(EventBus eventBus, String title) {
        this(eventBus, title, false);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    /**
     * Releases all handlers associated with this button definition.
     */
    public void releaseAllHandlers() {
        for (HandlerRegistration reg : handlerRegistrations) {
            reg.removeHandler();
        }
        handlerRegistrations.clear();
    }

    /**
     * Registers a handler that will be removed when calling {@link #releaseAllHandlers}.
     */
    protected void registerHandler(HandlerRegistration reg) {
        handlerRegistrations.add(reg);
    }

    @Override
    public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
        HandlerRegistration reg = eventBus.addHandlerToSource(InitializeEvent.getType(), this, handler);
        registerHandler(reg);
        return reg;
    }

    @Override
    public boolean isVisible(E mainEntity, List<T> selectedItems) {
        return true;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public void update() {
        // Trigger InitializeEvent to update corresponding button widget
        InitializeEvent.fire(this);
    }

    @Override
    public HasCssName getIcon() {
        return null;
    }

    @Override
    public String getText() {
        return title.asString();
    }

    @Override
    public boolean isSubTitledAction() {
        return subTitledAction;
    }

    @Override
    public SafeHtml getTooltip() {
        return null;
    }

    @Override
    public SafeHtml getMenuItemTooltip() {
        return null;
    }

    /**
     * This function returns the sub menu actions
     *
     * @return the sub menu actions
     */
    public List<ActionButtonDefinition<E, T>> getSubActions() {
        return new ArrayList<>(subActions);
    }

    @Override
    public IEventListener<? super PropertyChangedEventArgs> getUpdateOnModelChangeRelevantForActionsListener() {
        return updateOnModelChangeRelevantForActionsListener;
    }
}
