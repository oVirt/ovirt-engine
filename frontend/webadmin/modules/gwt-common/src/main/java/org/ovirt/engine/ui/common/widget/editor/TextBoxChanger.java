package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.user.client.ui.TextBox;

/**
 * A {@link TextBox} that uses a {@link ValueBoxEditorChanger} as the Editor
 */
public class TextBoxChanger extends TextBox {

    private ValueBoxEditorChanger<String> editor;

    @Override
    public ValueBoxEditorChanger<String> asEditor() {
        if (editor == null) {
            editor = ValueBoxEditorChanger.of(this);
        }
        return editor;
    }

}
