package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

/**
 * Interface implemented by Models that work with dedicated entity instance. Most notably EntityModel or
 * SearchableListModel with entity and list.
 *
 * @param <T>
 *            type of the containing Entity
 */
public interface HasEntity<T> extends IModel {

    EventDefinition entityChangedEventDefinition = new EventDefinition("EntityChanged", HasEntity.class); //$NON-NLS-1$

    Event<EventArgs> getEntityChangedEvent();

    T getEntity();

    void setEntity(T value);

    /**
     * Allows the entity container to be used as an option(maybe) container with 2 states:
     *
     * <pre>
     * <ol>
     *     <li>'entity present' (aka Some)
     *         <ul>
     *         <li>for backward compatibility it's default state</li>
     *         <li>contains value or null</li>
     *         </ul>
     *     </li>
     *     <li>'no entity present' (aka None)
     *        <ul>
     *        <li>the container is considered empty.</li>
     *        <li>due to backward compatibility the call to {@link #getEntity()} will not throw NoSuchElementException
     *            but will return whatever value is currently stored (which should be ignored as it's stale),</li>
     *       <li>empty container is not valid</li>
     *       </ul>
     *     </li>
     * </ol>
     * </pre>
     *
     * Background
     * </p>
     * <p>
     * If editor is in invalid state then the current value in the model should be removed. This is implemented as
     * resetting the value to 'null' and works well for required fields. However 'null' is a legal value for optional
     * fields and cannot be used as special 'no value' marker. In order to address this issue EntityModel container was
     * extended to support 'no entity present' state.
     * </p>
     *
     * @return <code>true</code> if this container contains value and <code>false</code> otherwise.
     */
    boolean isEntityPresent();

    void setEntityPresent(boolean flag);
}
