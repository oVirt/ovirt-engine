package org.ovirt.engine.ui.common.editor;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.impl.SimpleBeanEditorDelegate;

/**
 * An Editor Delegate for UiCommon Models
 *
 * @param <T>
 *            The Edited Type
 * @param <E>
 *            The Editor Type
 */
public abstract class UiCommonModelEditorDelegate<T, E extends Editor<T>> extends SimpleBeanEditorDelegate<T, E> {

    @Override
    protected boolean shouldFlush() {
        // Disable flush behavior
        return false;
    }

}
