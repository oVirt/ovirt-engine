package org.ovirt.engine.ui.common.widget;

import com.google.gwt.editor.client.IsEditor;

/**
 * Interface implemented by widgets that expose the Editor Driver functionality,
 * but do not wish to implement the {@link HasEditorDriver} interface directly.
 *
 * @param <T>
 *            The type being edited.
 */
public interface IsEditorDriver<T> extends IsEditor<HasEditorDriver<T>> {

}
