package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.view.client.CellPreviewEvent;

/**

 * This class may be used to display a ListBox, backed by a ListModel, in cell widgets. It mimics the behaviour of a
 * SelectionCell by passing on rendering requests and browser events to a member delegate. The SelectionCell options are
 * set by rendering the items of a ListModel using the renderer passed to the constructor. Additional functionality
 * includes enabling/disabling the ListBox according to the IsChangeable state of the ListModel.
 * </p>
 * <p>
 * Supports tooltips.
 * </p>
 *
 * @param <T>
 *            the ListModel item type.
 */
public class ListModelListBoxCell<T> extends AbstractInputCell<ListModel, String> implements EventHandlingCell {

    private static final String PATTERN_SELECT = "<select"; //$NON-NLS-1$
    private static final String REPLACEMENT_SELECT = "<select disabled"; //$NON-NLS-1$

    private SelectionCell delegate;
    private Map<String, T> entityByName;
    private final Renderer<T> renderer;

    public ListModelListBoxCell(final Renderer<T> renderer) {
        this.renderer = renderer;
        delegate = new SelectionCell(new ArrayList<String>()); // just to avoid null pointer in setSelection()
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CHANGE);
        return set;
    }

    @SuppressWarnings("unchecked")
    private void setOptions(ListModel model) {
        Iterable<T> items = model.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }
        entityByName = new HashMap<>();
        List<String> options = new ArrayList<>();

        for (T entity : items) {
            String entityName = renderer.render(entity);
            entityByName.put(entityName, entity);
            options.add(entityName);
        }
        delegate = new SelectionCell(options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(Context context, ListModel value, SafeHtmlBuilder sb, String id) {
        setOptions(value);
        SafeHtmlBuilder sbDelegate = new SafeHtmlBuilder();
        delegate.render(context, renderer.render((T) value.getSelectedItem()), sbDelegate);
        String select = sbDelegate.toSafeHtml().asString();
        select = select.replaceFirst(PATTERN_SELECT, PATTERN_SELECT + " id=\"" + id  + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (value.getIsChangable()) {
            sb.append(SafeHtmlUtils.fromTrustedString(select));
        } else {
            sb.appendHtmlConstant(select.replaceFirst(PATTERN_SELECT, REPLACEMENT_SELECT));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            final ListModel model,
            NativeEvent event,
            ValueUpdater<ListModel> valueUpdater) {
        delegate.onBrowserEvent(context,
                parent,
                renderer.render((T) model.getSelectedItem()),
                event,
                value -> {
                    if (value != null) {
                        model.setSelectedItem(entityByName.get(value));
                    }
                });
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return EventHandlingCellMixin.selectOptionHandlesClick(event);
    }

}
