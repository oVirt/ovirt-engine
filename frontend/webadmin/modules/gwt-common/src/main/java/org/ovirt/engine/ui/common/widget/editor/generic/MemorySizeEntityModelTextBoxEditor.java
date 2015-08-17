package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

/**
 * Composite Editor that uses {@link MemorySizeEntityModelTextBox}.
 */
public class MemorySizeEntityModelTextBoxEditor extends EntityModelTextBoxEditor<Integer> {

    public MemorySizeEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new MemorySizeEntityModelTextBox(), visibilityRenderer);
    }

    public MemorySizeEntityModelTextBoxEditor() {
        super(new MemorySizeEntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }
}
