package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.IntegerBox;

public class IntegerEntityModelEditor
        extends AbstractLabelableEntityModelEditor<Integer, IntegerBox> {

    public IntegerEntityModelEditor() {
        super(new IntegerBox());
    }

    /**
     * @return parsed integer or null
     */
    @Override
    public Integer getValue() {
        return getEditorWidget().getValue();
    }

    @Override
    public void setValue(Integer value) {
        getEditorWidget().setValue(value);
    }
}
