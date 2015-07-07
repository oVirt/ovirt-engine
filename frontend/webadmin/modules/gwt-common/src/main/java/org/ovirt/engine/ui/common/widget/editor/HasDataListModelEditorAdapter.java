package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.user.cellview.client.AbstractHasData;

/**
 * Adapts {@link HasEditorDriver} functionality to {@link AbstractHasData} widgets acting as {@link ListModel} Editors.
 *
 * @param <M>
 *            List model type.
 * @param <T>
 *            HasData widget's row data type.
 */
public class HasDataListModelEditorAdapter<M extends ListModel, T> implements HasEditorDriver<M> {

    private final AbstractHasData<T> hasDataWidget;

    private M listModel;

    public HasDataListModelEditorAdapter(AbstractHasData<T> hasDataWidget) {
        this.hasDataWidget = hasDataWidget;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(M object) {
        this.listModel = object;

        // Update row data when ListModel items change
        object.getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                M list = (M) sender;
                List<T> items = (List<T>) list.getItems();
                hasDataWidget.setRowData(items == null ? new ArrayList<T>() : items);
            }
        });

        // Update selection model when ListModel selection changes
        object.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                M list = (M) sender;
                hasDataWidget.getSelectionModel().setSelected((T) list.getSelectedItem(), true);
            }
        });
        object.getSelectedItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                M list = (M) sender;
                if (list.getSelectedItems() != null) {
                    for (Object item : list.getSelectedItems()) {
                        T entityModel = (T) item;
                        hasDataWidget.getSelectionModel().setSelected(entityModel, true);
                    }
                }
            }
        });

        // Get items from ListModel and update row data
        List<T> items = (List<T>) object.getItems();
        hasDataWidget.setRowData(items == null ? new ArrayList<T>() : items);
    }

    @Override
    public M flush() {
        return listModel;
    }

}
