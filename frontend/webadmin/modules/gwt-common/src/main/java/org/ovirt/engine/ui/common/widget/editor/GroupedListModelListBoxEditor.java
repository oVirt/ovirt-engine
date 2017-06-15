package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;

import com.google.gwt.editor.client.IsEditor;

public class GroupedListModelListBoxEditor<T>
    extends AbstractValidatedWidgetWithLabel<T, GroupedListModelListBox<T>>
    implements IsEditor<WidgetWithLabelEditor<T, GroupedListModelListBoxEditor<T>>>{

    private final WidgetWithLabelEditor<T, GroupedListModelListBoxEditor<T>> editor;

    public GroupedListModelListBoxEditor(GroupedListModelListBox<T> contentWidget) {
        super(contentWidget);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<T, GroupedListModelListBoxEditor<T>> asEditor() {
        return editor;
    }
}
