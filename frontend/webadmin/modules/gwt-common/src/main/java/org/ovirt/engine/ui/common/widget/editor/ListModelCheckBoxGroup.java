package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * ListModel bound CheckBoxGroup that extends {@link CheckBoxGroup}.
 */
public class ListModelCheckBoxGroup<T> extends CheckBoxGroup<T> implements EditorWidget<List<T>, TakesValueEditor<List<T>>>, HasConstrainedValue<List<T>> {

    private TakesConstrainedValueEditor<List<T>> editor;

    private char accessKey;

    /**
     * Constructor of ListModel bound CheckBoxGroup
     * @param renderer
     *            to render the values passed to ListModel's setItems and hence setAcceptableValues
     */
    public ListModelCheckBoxGroup(Renderer<T> renderer) {
        super(renderer);
    }

    @Override
    public TakesValueEditor<List<T>> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    public char getAccessKey() {
        return accessKey;
    }

    @Override
    public void setAccessKey(char key) {
        this.accessKey = key;
    }

    @Override
    public void setFocus(boolean focused) {
    }
}
