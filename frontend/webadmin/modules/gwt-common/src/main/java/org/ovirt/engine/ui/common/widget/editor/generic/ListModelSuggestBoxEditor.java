package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;

import com.google.gwt.editor.client.IsEditor;

/**
 * Composite Editor that uses {@link ListModelSuggestBox}.
 *
 */
public class ListModelSuggestBoxEditor extends AbstractValidatedWidgetWithLabel<String, ListModelSuggestBox>
        implements IsEditor<WidgetWithLabelEditor<String, ListModelSuggestBoxEditor>>, HasCleanup {

    private final WidgetWithLabelEditor<String, ListModelSuggestBoxEditor> editor;

    public ListModelSuggestBoxEditor() {
        super(new ListModelSuggestBox());
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<String, ListModelSuggestBoxEditor> asEditor() {
        return editor;
    }

}
