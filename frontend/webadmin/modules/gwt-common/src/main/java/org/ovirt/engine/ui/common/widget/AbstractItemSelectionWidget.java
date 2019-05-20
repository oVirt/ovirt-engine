package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.SuggestionMatcher;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * This model-backed widget can be used to provide a type ahead list box with a button that can be used to trigger an action.
 *
 * @param <T> the type of values contained in the listbox editor
 */
public abstract class AbstractItemSelectionWidget<T extends Nameable> extends Composite implements IsEditor<WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>>> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractItemSelectionWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    @UiField
    Label filterListLabel;

    @UiField
    WidgetTooltip filterListLabelTooltip;

    @UiField(provided=true)
    protected ListModelTypeAheadListBoxEditor<T> filterListEditor;

    @UiField
    Button addSelectedItemButton;

    public AbstractItemSelectionWidget() {
        filterListEditor = createFilterListEditor();
        filterListEditor.hideLabel();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addSelectedItemButton.addStyleName(Styles.PULL_RIGHT);
    }

    public ListModelTypeAheadListBoxEditor<T> getFilterListEditor() {
        return filterListEditor;
    }

    private ListModelTypeAheadListBoxEditor<T> createFilterListEditor() {
        return new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<T>() {
                    @Override
                    public String getReplacementStringNullSafe(T item) {
                        return item.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(T item) {
                        return typeAheadNameTemplateNullSafe(item.getName());
                    }
                },
                new VisibilityRenderer.SimpleVisibilityRenderer(),
                new SuggestionMatcher.ContainsSuggestionMatcher());
    }

    private String typeAheadNameTemplateNullSafe(String name) {
        if (StringHelper.isNotNullOrEmpty(name)) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }

    public HasClickHandlers getAddSelectedItemButton() {
        return addSelectedItemButton;
    }

    public WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>> asEditor() {
        return filterListEditor.asEditor();
    }
}
