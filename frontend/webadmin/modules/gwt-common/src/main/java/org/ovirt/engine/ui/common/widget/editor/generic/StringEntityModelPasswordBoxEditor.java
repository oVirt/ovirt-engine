package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelPasswordBoxEditor extends EntityModelPasswordBoxEditor<String> {

    public static final String AUTOCOMPLETE_NEW_PASSWORD = "new-password"; //$NON-NLS-1$

    public StringEntityModelPasswordBoxEditor() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }

    public void setAutocomplete(String value) {
        getContentWidget().getElement().setAttribute("autocomplete", value); //$NON-NLS-1$
    }
}
