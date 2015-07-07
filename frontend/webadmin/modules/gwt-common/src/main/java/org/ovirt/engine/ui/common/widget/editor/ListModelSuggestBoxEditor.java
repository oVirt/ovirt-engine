package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import com.google.gwt.editor.client.IsEditor;

/**
 * Composite Editor that uses {@link ListModelSuggestBox}.
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor instead
 */
@Deprecated
public class ListModelSuggestBoxEditor extends AbstractValidatedWidgetWithLabel<Object, ListModelSuggestBox>
        implements IsEditor<WidgetWithLabelEditor<Object, ListModelSuggestBoxEditor>> {

    private final WidgetWithLabelEditor<Object, ListModelSuggestBoxEditor> editor;

    public ListModelSuggestBoxEditor() {
        super(new ListModelSuggestBox());
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<Object, ListModelSuggestBoxEditor> asEditor() {
        return editor;
    }

}
