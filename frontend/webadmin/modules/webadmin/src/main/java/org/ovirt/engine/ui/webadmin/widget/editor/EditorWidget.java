package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A Widget that can be used in Editors
 *
 * @param <T>
 *            The Object Type
 * @param <E>
 *            The Editor Type
 */
public interface EditorWidget<T, E extends Editor<T>> extends IsWidget, HasAllKeyHandlers, HasEnabled, Focusable, IsEditor<E> {

}
