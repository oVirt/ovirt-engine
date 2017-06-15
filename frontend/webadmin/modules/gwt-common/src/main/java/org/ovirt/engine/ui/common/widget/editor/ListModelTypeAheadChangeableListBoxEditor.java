package org.ovirt.engine.ui.common.widget.editor;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

import com.google.gwt.editor.client.IsEditor;

/**
 * Composite Editor that uses {@link ListModelTypeAheadChangeableListBox}.
 */
public class ListModelTypeAheadChangeableListBoxEditor extends AbstractValidatedWidgetWithLabel<String, ListModelTypeAheadChangeableListBox>
        implements IsEditor<WidgetWithLabelEditor<String, ListModelTypeAheadChangeableListBoxEditor>> {

    private final WidgetWithLabelEditor<String, ListModelTypeAheadChangeableListBoxEditor> editor;

    public ListModelTypeAheadChangeableListBoxEditor(ListModelTypeAheadListBoxEditor.SuggestBoxRenderer<String> renderer) {
        this(renderer, true);
    }

    public ListModelTypeAheadChangeableListBoxEditor(ListModelTypeAheadListBoxEditor.SuggestBoxRenderer<String> renderer, boolean autoAddToValidValues) {
        this(renderer, autoAddToValidValues, new VisibilityRenderer.SimpleVisibilityRenderer(), ""); //$NON-NLS-1$
    }

    public ListModelTypeAheadChangeableListBoxEditor(ListModelTypeAheadListBoxEditor.SuggestBoxRenderer<String> renderer, VisibilityRenderer visibilityRenderer) {
        this(renderer, true, visibilityRenderer, ""); //$NON-NLS-1$
    }

    public ListModelTypeAheadChangeableListBoxEditor(ListModelTypeAheadListBoxEditor.SuggestBoxRenderer<String> renderer,
            boolean autoAddToValidValues,
            VisibilityRenderer visibilityRenderer,
            String nullReplacementText) {
        super(new ListModelTypeAheadChangeableListBox(renderer, autoAddToValidValues, nullReplacementText), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<String, ListModelTypeAheadChangeableListBoxEditor> asEditor() {
        return editor;
    }

    @Override
    public void setUsePatternFly(final boolean usePatternfly) {
        super.setUsePatternFly(usePatternfly);
        removeContentWidgetStyleName(Styles.FORM_CONTROL);
    }

    @Override
    public ListModelTypeAheadChangeableListBox asWidget() {
        return getContentWidget();
    }

    public abstract static class NullSafeSuggestBoxRenderer extends ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<String> {
        @Override
        public String getReplacementStringNullSafe(String data) {
            return data; // for string objects the replacement value is the object itself (null safe)
        }
    }

    public void setNullReplacementString(String nullReplacementString) {
        super.getContentWidget().setNullReplacementString(nullReplacementString);
    }
}
