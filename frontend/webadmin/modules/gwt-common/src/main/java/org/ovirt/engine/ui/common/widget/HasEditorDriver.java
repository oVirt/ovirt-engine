package org.ovirt.engine.ui.common.widget;

import com.google.gwt.editor.client.Editor;

/**
 * Widgets that implement this interface are Editors that expose the Editor Driver functionality.
 *
 * @param <T>
 *            The type being edited.
 */
public interface HasEditorDriver<T> extends Editor<T> {

    /**
     * Initialize the Editor from the given object.
     */
    void edit(T object);

    /**
     * Returns an object updated according to the current state of the Editor.
     */
    T flush();

}
