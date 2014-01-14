package org.ovirt.engine.ui.common.editor;

import org.ovirt.engine.ui.common.widget.HasAccess;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.HasValidation;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.user.client.ui.Focusable;

/**
 * Classes acting as Editors of UiCommon model properties can implement this interface to expose additional
 * functionality to {@link UiCommonEditorVisitor}.
 *
 * @param <T>
 *            The type being edited.
 */
public interface UiCommonEditor<T> extends Editor<T>,
        HasValidation, HasEnabledWithHints, HasAccess, HasAllKeyHandlers, Focusable {

    /**
     * In case of leaf Editor, returns {@code this}. In case of composite Editor, returns the actual leaf Editor for
     * editing the given property.
     */
    LeafValueEditor<T> getActualEditor();

}
