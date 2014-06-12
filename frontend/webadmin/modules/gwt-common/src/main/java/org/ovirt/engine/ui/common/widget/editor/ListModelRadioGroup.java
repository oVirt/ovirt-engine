package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * List box widget that adapts to UiCommon list model items.
 *
 * @param <T>
 *            Radio box item type.
 */
public class ListModelRadioGroup<T> extends RadioGroup<T> implements EditorWidget<T, TakesValueEditor<T>>, HasConstrainedValue<T> {

    private TakesConstrainedValueEditor<T> editor;

    /**
     * Creates a list box that renders its items using the specified {@link Renderer}.
     *
     * @param renderer
     *            Renderer for list box items.
     */
    public ListModelRadioGroup(Renderer<T> renderer) {
        super(renderer);
    }

    @Override
    public TakesConstrainedValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

}
