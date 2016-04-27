package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.TakesValue;

public class AbstractValueBoxWithIconEditor<T, W extends EditorWidget<T, ValueBoxEditor<T>> & TakesValue<T> & HasValueChangeHandlers<T>> extends AbstractValidatedWidgetWithIcon<T, W> implements IsEditor<WidgetWithLabelEditor<T, AbstractValueBoxWithIconEditor<T, W>>> {

    private final WidgetWithLabelEditor<T, AbstractValueBoxWithIconEditor<T, W>> editor;

    public AbstractValueBoxWithIconEditor(W contentWidget, VisibilityRenderer visibilityRenderer, String iconName) {
        super(contentWidget, visibilityRenderer, iconName);
        this.editor = WidgetWithLabelEditor.of(contentWidget.asEditor(), this);
    }

    public AbstractValueBoxWithIconEditor(W contentWidget, String iconName) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer(), iconName);
    }

    public W asValueBox() {
        return getContentWidget();
    }

    @Override
    public WidgetWithLabelEditor<T, AbstractValueBoxWithIconEditor<T, W>> asEditor() {
        return editor;
    }

    public void setAutoComplete(String value) {
        asValueBox().asWidget().getElement().setAttribute("autocomplete", "off"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setPlaceHolder(String holder) {
        getContentWidgetElement().setPropertyString("placeholder", holder);//$NON-NLS-1$
    }

}
