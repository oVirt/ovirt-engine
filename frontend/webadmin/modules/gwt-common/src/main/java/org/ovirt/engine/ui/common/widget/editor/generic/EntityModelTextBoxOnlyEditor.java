package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Widget;

/*
 * Represents a Model bound editor containing only a TextBox with no label.
 */
public class EntityModelTextBoxOnlyEditor<T> extends AbstractValidatedWidgetWithLabel<T, EntityModelTextBox<T>>
        implements IsEditor<WidgetWithLabelEditor<T, EntityModelTextBoxOnlyEditor<T>>> {

    private final WidgetWithLabelEditor<T, EntityModelTextBoxOnlyEditor<T>> editor;

    public EntityModelTextBoxOnlyEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelTextBox<>(renderer, parser), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public EntityModelTextBoxOnlyEditor(EntityModelTextBox<T> textBox, VisibilityRenderer visibilityRenderer) {
        super(textBox, visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);

        // Fix styles.
        if (!isUsePatternfly()) {
            getFormLabel().asWidget().setVisible(false);
            com.google.gwt.dom.client.Style panelStyle = getContentWidgetContainer().getElement().getStyle();
            panelStyle.setFloat(com.google.gwt.dom.client.Style.Float.NONE);
        }
    }

    @Override
    public WidgetWithLabelEditor<T, EntityModelTextBoxOnlyEditor<T>> asEditor() {
        return editor;
    }

    public EntityModelTextBox<T> asValueBox() {
        return getContentWidget();
    }
}
