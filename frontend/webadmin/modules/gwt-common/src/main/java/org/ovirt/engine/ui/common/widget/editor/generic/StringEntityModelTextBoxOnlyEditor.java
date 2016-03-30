package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

public class StringEntityModelTextBoxOnlyEditor extends EntityModelTextBoxOnlyEditor<String> {

    public StringEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(new StringEntityModelTextBox(), visibilityRenderer);
    }

    public StringEntityModelTextBoxOnlyEditor() {
        super(new StringEntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    public void setWidgetColSize(ColumnSize size) {
        addWrapperStyleName(size.getCssName());
    }
}
