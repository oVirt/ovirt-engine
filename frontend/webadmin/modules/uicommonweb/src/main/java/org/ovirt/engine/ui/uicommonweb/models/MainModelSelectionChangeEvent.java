package org.ovirt.engine.ui.uicommonweb.models;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class MainModelSelectionChangeEvent extends GwtEvent<MainModelSelectionChangeEvent.MainModelSelectionChangeHandler> {

    SearchableListModel<? extends EntityModel<?>> mainModel;

    protected MainModelSelectionChangeEvent() {
        // Possibly for serialization.
    }

    public MainModelSelectionChangeEvent(SearchableListModel<? extends EntityModel<?>> mainModel) {
        this.mainModel = mainModel;
    }

    public static void fire(HasHandlers source, SearchableListModel<? extends EntityModel<?>> mainModel) {
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

    private static final Type<MainModelSelectionChangeHandler> TYPE = new Type<MainModelSelectionChangeHandler>();

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

    public SearchableListModel<? extends EntityModel<?>> getMainModel() {
        return mainModel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MainModelSelectionChangeEvent other = (MainModelSelectionChangeEvent) obj;
        if (mainModel == null) {
            if (other.mainModel != null) {
                return false;
            }
        } else if (!mainModel.equals(other.mainModel)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 23;
        hashCode = (hashCode * 37) + (mainModel == null ? 1 : mainModel.hashCode());
        return hashCode;
    }

    @Override
    public String toString() {
        return "MainModelSelectionChangeEvent[" //$NON-NLS-1$
                + mainModel
                + "]"; //$NON-NLS-1$
    }
}
