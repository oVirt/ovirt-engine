package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithIcon;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;

public class ListModelListBoxWithIconEditor<T> extends AbstractValidatedWidgetWithIcon<T, ListModelListBox<T>> implements IsEditor<WidgetWithLabelEditor<T, ListModelListBoxWithIconEditor<T>>> {

    private final WidgetWithLabelEditor<T, ListModelListBoxWithIconEditor<T>> editor;

    public ListModelListBoxWithIconEditor() {
        this(new StringRenderer<T>(), "fa fa-puzzle-piece");//$NON-NLS-1$
        addStyleNameToIcon("prf-icon-color");//$NON-NLS-1$
    }

    public ListModelListBoxWithIconEditor(VisibilityRenderer visibilityRenderer, String iconName) {
        this(new StringRenderer<T>(), visibilityRenderer, iconName);
    }

    public ListModelListBoxWithIconEditor(Renderer<T> renderer,
            VisibilityRenderer visibilityRenderer,
            String iconName) {
        super(new ListModelListBox<T>(renderer), visibilityRenderer, iconName);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public ListModelListBoxWithIconEditor(Renderer<T> renderer, String iconName) {
        this(renderer, new VisibilityRenderer.SimpleVisibilityRenderer(), iconName);
    }

    public ListBox asListBox() {
        return getContentWidget().asListBox();
    }

    @Override
    public WidgetWithLabelEditor<T, ListModelListBoxWithIconEditor<T>> asEditor() {
        return editor;
    }

}
