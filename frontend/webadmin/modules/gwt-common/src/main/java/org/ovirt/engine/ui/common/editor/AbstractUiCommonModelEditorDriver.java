package org.ovirt.engine.ui.common.editor;

import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.impl.BaseEditorDriver;

/**
 * Base class for generated UiCommonEditorDriver implementations for editing EntityModel and ListModel instances.
 *
 * @param <T> the type being edited
 * @param <E> the Editor type
 */
public abstract class AbstractUiCommonModelEditorDriver<T extends Model, E extends Editor<T>>
        extends BaseEditorDriver<T, E> implements UiCommonEditorDriver<T, E> {

    private UiCommonListenerMap listenerMap;
    private UiCommonEventMap eventMap;
    private Map<String, Model> ownerModels;

    private IEventListener<PropertyChangedEventArgs> propertyChangeListener;
    private final UiCommonEditorVisitor visitor = new UiCommonEditorVisitor();

    /**
     * {@inheritDoc} <BR>
     * Register listeners for EntityModel changes, according to the Event Map created by the Driver
     */
    @Override
    public void edit(T object) {
        // Cleanup on previously edited object
        removePropertyChangeListener(getObject());

        // Initialize Editor Driver with newly edited object
        doEdit(object);
        updateUiCommonMaps();
        updateUiCommonEditorVisitor();
        updatePropertyChangeListener(object);

        // Traverse Editor hierarchy to handle UiCommon specifics
        accept(visitor);
    }

    private void cleanupUiCommonMaps() {
        if (listenerMap != null) {
            listenerMap.clear();
        }
        if (eventMap != null) {
            eventMap.clear();
        }
        if (ownerModels != null) {
            ownerModels.clear();
        }
    }

    private void updateUiCommonMaps() {
        cleanupUiCommonMaps();

        // Map creator methods depend on currently edited object
        listenerMap = getListenerMap();
        eventMap = getEventMap();
        ownerModels = getOwnerModels();
    }

    private void updateUiCommonEditorVisitor() {
        visitor.setEventMap(eventMap);
        visitor.setOwnerModels(ownerModels);
    }

    private void removePropertyChangeListener(T object) {
        if (object != null) {
            object.getPropertyChangedEvent().removeListener(propertyChangeListener);
        }
    }

    /**
     * Register a "PropertyChangedEvent" to get Model changes.
     */
    private void updatePropertyChangeListener(T object) {
        removePropertyChangeListener(object);

        propertyChangeListener = (ev, sender, args) -> {
            String propName = args.propertyName;
            listenerMap.callListener(propName, "PropertyChanged"); //$NON-NLS-1$
        };

        if (object != null) {
            object.getPropertyChangedEvent().addListener(propertyChangeListener);
        }
    }

    @Override
    public T flush() {
        doFlush();
        return getObject();
    }

    @Override
    public void initialize(E editor) {
        doInitialize(editor);
    }

    /**
     * Returns a {@link UiCommonListenerMap} that contains a PropertyChanged listener
     * for each property defined by the edited Model.
     */
    protected abstract UiCommonListenerMap getListenerMap();

    /**
     * Returns a {@link UiCommonEventMap} for the edited Model.
     */
    protected abstract UiCommonEventMap getEventMap();

    /**
     * Returns a Map of the parent ListModel for all the ListModelBoxes being edited.
     */
    protected abstract Map<String, Model> getOwnerModels();

    /**
     * Clean up the Editor Driver itself.
     */
    protected void cleanupEditorDriver() {
        // At this point, the model is already cleaned up
        cleanupUiCommonMaps();
        removePropertyChangeListener(getObject());
    }

}
