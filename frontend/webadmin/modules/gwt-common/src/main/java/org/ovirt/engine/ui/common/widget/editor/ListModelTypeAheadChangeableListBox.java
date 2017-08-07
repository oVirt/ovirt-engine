package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor.SuggestBoxRenderer;

/**
 * SuggestBox widget that adapts to UiCommon list model items and looks like a list box. The suggestion content can be rich (html).
 * <p>
 * Allows the user to edit the textbox without any enforcement.
 */
public class ListModelTypeAheadChangeableListBox extends ListModelTypeAheadListBox<String> {

    private String nullReplacementText;

    public ListModelTypeAheadChangeableListBox(SuggestBoxRenderer<String> renderer) {
        this(renderer, true, null);
    }

    public ListModelTypeAheadChangeableListBox(SuggestBoxRenderer<String> renderer, boolean autoAddToValidValues, String nullReplacementText) {
        super(renderer, autoAddToValidValues, new SuggestionMatcher.StartWithSuggestionMatcher());
        if (nullReplacementText == null) {
            this.nullReplacementText = ""; //$NON-NLS-1$
        } else {
            this.nullReplacementText = nullReplacementText;
        }
    }

    @Override
    protected void adjustSelectedValue() {
        String providedText = asSuggestBox().getText();
        String newData = asEntity(providedText);
        setValue(newData);
    }

    @Override
    protected String asEntity(String provided) {
        return provided == null ? "" : provided;
    }

    @Override
    protected void render(String value, boolean fireEvents) {
        boolean isEmptyReplace =
                StringHelper.isNullOrEmpty(value) || value.equals(nullReplacementText);
        // handle null replacement
        asSuggestBox().setValue(isEmptyReplace ? nullReplacementText : value, fireEvents);
        grayOutPlaceholderText(isEmptyReplace);
    }

    protected void setNullReplacementString(String nullReplacementText) {
        this.nullReplacementText = nullReplacementText;
    }
}
