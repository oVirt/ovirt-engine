package org.ovirt.engine.ui.common.editor;

import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.impl.BaseEditorDriver;

/**
 * A base implementation class for generated SimpleBeanEditorDriver implementations based on EntityModel and ListModel
 * instances.
 *
 * @param <T>
 *            the type being edited
 * @param <E>
 *            the Editor type
 */
public abstract class AbstractUiCommonModelEditorDriver<T extends Model, E extends Editor<T>>
        extends BaseEditorDriver<T, E> implements SimpleBeanEditorDriver<T, E> {

    /**
     * {@inheritDoc} <BR>
     * Register listeners for EntityModel changes, according to the Event Map created by the Driver
     */
    @Override
    public void edit(T object) {
        doEdit(object);

        final UiCommonListenerMap listenerMap = getListenerMap();

        // Register a "PropertyChangedEvent" to get Model changes
        object.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                listenerMap.callListener(propName, "PropertyChanged"); //$NON-NLS-1$
            }
        });

        // Visit editors
        accept(new UiCommonEditorVisitor<T>(getEventMap(), getOwnerModels()));
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
     * Returns a {@link UiCommonListenerMap} that contains a PropertyChanged Listener for each Property in the edited
     * Model
     *
     * @return
     */
    protected abstract UiCommonListenerMap getListenerMap();

    /**
     * Returns a {@link UiCommonEventMap} for the edited Model
     *
     * @return
     */
    protected abstract UiCommonEventMap getEventMap();

    /**
     * Returns a Map of the parent ListModel for all the ListModelBoxes being edited.
     *
     * @param absolutePath
     * @return
     */
    protected abstract Map<String, EntityModel> getOwnerModels();

}
