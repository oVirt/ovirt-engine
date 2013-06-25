package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Widget;

/*
 * Represents a Model bound editor containing only a TextBox with no label.
 */
public class EntityModelTextBoxOnlyEditor extends AbstractValidatedWidgetWithLabel<Object, EntityModelTextBox>
        implements IsEditor<WidgetWithLabelEditor<Object, EntityModelTextBoxOnlyEditor>> {

    private final WidgetWithLabelEditor<Object, EntityModelTextBoxOnlyEditor> editor;

    public EntityModelTextBoxOnlyEditor(Renderer<Object> renderer, Parser<Object> parser) {
        this(new EntityModelTextBox(renderer, parser), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public EntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        this(new EntityModelTextBox(), visibilityRenderer);
    }

    public EntityModelTextBoxOnlyEditor() {
        this(new EntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public EntityModelTextBoxOnlyEditor(EntityModelTextBox textBox, VisibilityRenderer visibilityRenderer) {
        super(textBox, visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);

        // Fix styles.
        com.google.gwt.dom.client.Style labelStyle = getLabelElement().getStyle();
        labelStyle.setDisplay(com.google.gwt.dom.client.Style.Display.NONE);

        // Style textBoxStyle = getContentWidgetElement().getStyle();
        // textBoxStyle.setWidth(240, Style.Unit.PX);

        com.google.gwt.dom.client.Style panelStyle = getContentWidgetContainer().getElement().getStyle();
        panelStyle.setFloat(com.google.gwt.dom.client.Style.Float.NONE);
    }

    @Override
    public WidgetWithLabelEditor<Object, EntityModelTextBoxOnlyEditor> asEditor() {
        return editor;
    }

    public EntityModelTextBox asValueBox() {
        return getContentWidget();
    }
}
