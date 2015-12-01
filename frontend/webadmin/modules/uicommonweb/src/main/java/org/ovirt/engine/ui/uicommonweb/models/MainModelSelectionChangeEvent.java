package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class MainModelSelectionChangeEvent extends GwtEvent<MainModelSelectionChangeEvent.MainModelSelectionChangeHandler> {

    SearchableListModel<?, ? extends EntityModel<?>> mainModel;

    protected MainModelSelectionChangeEvent() {
        // Possibly for serialization.
    }

    public MainModelSelectionChangeEvent(SearchableListModel<?, ? extends EntityModel<?>> mainModel) {
        this.mainModel = mainModel;
    }

    public static void fire(HasHandlers source, SearchableListModel<?, ? extends EntityModel<?>> mainModel) {
        MainModelSelectionChangeEvent eventInstance = new MainModelSelectionChangeEvent(mainModel);
        source.fireEvent(eventInstance);
    }

    public static void fire(HasHandlers source, MainModelSelectionChangeEvent eventInstance) {
        source.fireEvent(eventInstance);
    }

    public interface HasMainModelSelectionChangeHandlers extends HasHandlers {
        HandlerRegistration addMainModelSelectionChangeHandler(MainModelSelectionChangeHandler handler);
    }

    public interface MainModelSelectionChangeHandler extends EventHandler {
        public void onMainModelSelectionChange(MainModelSelectionChangeEvent event);
    }

    private static final Type<MainModelSelectionChangeHandler> TYPE = new Type<>();

    public static Type<MainModelSelectionChangeHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<MainModelSelectionChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MainModelSelectionChangeHandler handler) {
        handler.onMainModelSelectionChange(this);
    }

    public SearchableListModel<?, ? extends EntityModel<?>> getMainModel() {
        return mainModel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MainModelSelectionChangeEvent)) {
            return false;
        }
        MainModelSelectionChangeEvent other = (MainModelSelectionChangeEvent) obj;
        return Objects.equals(mainModel, other.mainModel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mainModel);
    }

    @Override
    public String toString() {
        return "MainModelSelectionChangeEvent[" //$NON-NLS-1$
                + mainModel
                + "]"; //$NON-NLS-1$
    }
}
