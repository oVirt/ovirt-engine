package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

import com.google.gwt.text.shared.Parser;

public class StringEntityModelTextBoxOnlyEditor extends EntityModelTextBoxOnlyEditor<String> {

    public StringEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(new StringEntityModelTextBox(), visibilityRenderer);
    }

    public StringEntityModelTextBoxOnlyEditor() {
        super(new StringEntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public StringEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer, Parser<String> parser) {
        super(new StringEntityModelTextBox(parser), visibilityRenderer);
    }

    private StringEntityModelTextBoxOnlyEditor(EntityModelTextBox<String> textBox, VisibilityRenderer visibilityRenderer) {
        super(textBox, visibilityRenderer);
    }

    public static StringEntityModelTextBoxOnlyEditor newTrimmingEditor() {
        return  newTrimmingEditor(new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public static StringEntityModelTextBoxOnlyEditor newTrimmingEditor(VisibilityRenderer visibilityRenderer) {
        return new StringEntityModelTextBoxOnlyEditor(
                new StringEntityModelTextBox(ToStringEntityModelParser.newTrimmingParser()),
                visibilityRenderer);
    }



    @Override
    public void setWidgetColSize(ColumnSize size) {
        addWrapperStyleName(size.getCssName());
    }
}
