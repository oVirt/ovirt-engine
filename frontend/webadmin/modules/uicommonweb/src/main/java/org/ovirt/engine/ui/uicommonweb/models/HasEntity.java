package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

/**
 * Interface implemented by Models that work with dedicated entity instance.
 * Most notably EntityModel or SearchableListModel with entity and list.
 *
 * @param <T> type of the containing Entity
 */
public interface HasEntity<T> extends IModel {

    EventDefinition entityChangedEventDefinition = new EventDefinition("EntityChanged", HasEntity.class); //$NON-NLS-1$;

    Event<EventArgs> getEntityChangedEvent();

    T getEntity();

    void setEntity(T value);
}
