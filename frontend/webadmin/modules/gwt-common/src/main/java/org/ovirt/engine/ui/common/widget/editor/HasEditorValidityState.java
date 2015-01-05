package org.ovirt.engine.ui.common.widget.editor;

public interface HasEditorValidityState {
    /**
     * Returns true if the current value of the editor is valid. For instance if an {@code Integer} editor has
     * a '1' in it. Returns false if the editor is invalid, for instance if an {@code Integer} editor has 'aa' in it
     * @return true is the editor is valid, false otherwise.
     */
    boolean isStateValid();
}
