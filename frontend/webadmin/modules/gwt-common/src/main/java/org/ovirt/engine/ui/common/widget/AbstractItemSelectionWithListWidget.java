package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * This model-backed widget can be used to provide a selection widget (type ahead listbox with button) and a display
 * widget (horizontal, unordered list).
 *
 * @param <T> the type of values contained in the listbox editor and list widget
 */
public abstract class AbstractItemSelectionWithListWidget<T> extends Composite implements IsEditor<WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>>> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractItemSelectionWithListWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided=true)
    protected AbstractItemSelectionWidget<T> selectionWidget;

    @UiField(provided=true)
    protected AbstractItemListWidget<ListModel<T>, T> listWidget;

    public AbstractItemSelectionWithListWidget() {
        selectionWidget = initItemSelectionWidget();
        listWidget = initItemListWidget();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    public abstract AbstractItemSelectionWidget<T> initItemSelectionWidget();

    public abstract AbstractItemListWidget<ListModel<T>, T> initItemListWidget();

    public WidgetWithLabelEditor<T, ListModelTypeAheadListBoxEditor<T>> asEditor() {
        return selectionWidget.asEditor();
    }

    public AbstractItemListWidget<ListModel<T>, T> getListWidget() {
        return listWidget;
    }

    public AbstractItemSelectionWidget<T> getSelectionWidget() {
        return selectionWidget;
    }

    public Button getAddSelectedItemButton() {
        return (Button) selectionWidget.getAddSelectedItemButton();
    }
}
