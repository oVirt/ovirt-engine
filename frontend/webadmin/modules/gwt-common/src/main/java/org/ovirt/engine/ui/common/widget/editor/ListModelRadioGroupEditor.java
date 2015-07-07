package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link ListModelRadioGroup}.
 *
 * @param <T>
 *            Radio box item type.
 */
public class ListModelRadioGroupEditor<T> extends AbstractValidatedWidgetWithLabel<T, ListModelRadioGroup<T>>
        implements IsEditor<WidgetWithLabelEditor<T, ListModelRadioGroupEditor<T>>> {

    private final WidgetWithLabelEditor<T, ListModelRadioGroupEditor<T>> editor;

    public ListModelRadioGroupEditor() {
        this(new StringRenderer<T>());
    }

    public ListModelRadioGroupEditor(VisibilityRenderer visibilityRenderer) {
        this(new StringRenderer<T>(), visibilityRenderer);
    }

    public ListModelRadioGroupEditor(Renderer<T> renderer, VisibilityRenderer visibilityRenderer) {
        super(new ListModelRadioGroup<T>(renderer), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public ListModelRadioGroupEditor(Renderer<T> renderer) {
        this(renderer, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public RadioGroup<T> asRadioGroup() {
        return getContentWidget();
    }

    @Override
    public WidgetWithLabelEditor<T, ListModelRadioGroupEditor<T>> asEditor() {
        return editor;
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
    }

}
