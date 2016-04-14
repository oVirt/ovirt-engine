package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.TextBox;

public class TextEntityModelEditor
        extends AbstractLabelableEntityModelEditor<String, TextBox> {

    public TextEntityModelEditor() {
        super(new TextBox());
    }

    /**
     * @return never `null`
     */
    @Override
    public String getValue() {
        return getEditorWidget().getValue();
    }

    /**
     * @param value `null` ~ ""
     */
    @Override
    public void setValue(String value) {
        getEditorWidget().setValue(value);
    }
}
