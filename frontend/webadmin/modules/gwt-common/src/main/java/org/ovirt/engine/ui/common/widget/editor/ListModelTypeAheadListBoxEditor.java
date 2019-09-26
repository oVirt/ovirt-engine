package org.ovirt.engine.ui.common.widget.editor;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;

import com.google.gwt.editor.client.IsEditor;

/**
 * Composite Editor that uses {@link ListModelTypeAheadListBox}.
 * @param <T>
 *            SuggestBox item type.
 */
public class ListModelTypeAheadListBoxEditor<T> extends AbstractValidatedWidgetWithLabel<T, ListModelTypeAheadListBox<T>>
        implements IsEditor<WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>>>, HasCleanup {

    private final WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>> editor;

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer) {
        this(renderer, true);
    }

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer, boolean autoAddToValidValues) {
        this(renderer, autoAddToValidValues, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer, VisibilityRenderer visibilityRenderer) {
        this(renderer, true, visibilityRenderer);
    }

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer, VisibilityRenderer visibilityRenderer,
                                           SuggestionMatcher suggestionMatcher) {
        this(renderer, true, visibilityRenderer, suggestionMatcher);
    }

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer,
                                           boolean autoAddToValidValues,
                                           VisibilityRenderer visibilityRenderer) {
        this(renderer, autoAddToValidValues, visibilityRenderer, new SuggestionMatcher.StartWithSuggestionMatcher());
    }

    public ListModelTypeAheadListBoxEditor(SuggestBoxRenderer<T> renderer,
            boolean autoAddToValidValues,
            VisibilityRenderer visibilityRenderer,
            SuggestionMatcher suggestionMatcher) {
        super(new ListModelTypeAheadListBox<>(renderer, autoAddToValidValues, suggestionMatcher), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public ListModelTypeAheadListBoxEditor(ListModelTypeAheadListBox<T> listBox,
            VisibilityRenderer visibilityRenderer) {
        super(listBox, visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>> asEditor() {
        return editor;
    }

    @Override
    public void setUsePatternFly(final boolean usePatternfly) {
        super.setUsePatternFly(usePatternfly);
        removeContentWidgetStyleName(Styles.FORM_CONTROL);
    }

    /**
     * A renderer for the suggest box. Receives an instance of the EntityModel and returns two kinds of the rendering.
     */
    public interface SuggestBoxRenderer<T> {
        /**
         * Returns the string that will be shown in the text box (not the suggestions list). Can be only clean string.
         * <p>
         * The following has to be true for each item from the underlying list model:
         * data1 != data2 => getReplacementString(data1) != getReplacementString(data2)
         */
        String getReplacementString(T data);

        /**
         * The string which is displayed as a suggestion. Can be rich - can contain html. There are no invariants - can
         * return anything.
         */
        String getDisplayString(T data);

    }

    @Override
    public ListModelTypeAheadListBox<T> asWidget() {
        return getContentWidget();
    }

    public abstract static class NullSafeSuggestBoxRenderer<T> implements SuggestBoxRenderer<T> {

        private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

        @Override
        public String getReplacementString(T data) {
            return emptyOr(data == null ? "" : getReplacementStringNullSafe(data)); //$NON-NLS-1$
        }

        @Override
        public String getDisplayString(T data) {
            return emptyOr(data == null ? templates.typeAheadEmptyContent().asString() : getDisplayStringNullSafe(data));
        }

        private String emptyOr(String string) {
            return string == null ? "" : string; //$NON-NLS-1$
        }

        public abstract String getReplacementStringNullSafe(T data);

        public abstract String getDisplayStringNullSafe(T data);

    }
}
