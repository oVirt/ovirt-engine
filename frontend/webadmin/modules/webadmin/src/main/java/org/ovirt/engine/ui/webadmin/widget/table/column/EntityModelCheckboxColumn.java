package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

public abstract class EntityModelCheckboxColumn extends Column<EntityModel, Boolean> {

    static class ClickableCheckboxCell extends CheckboxCell {

        private Set<String> consumedEvents;

        public ClickableCheckboxCell() {
            super(true, false);
            consumedEvents = new HashSet<String>(super.getConsumedEvents());
            // also consume click events (happens during selection)
            consumedEvents.add("click"); //$NON-NLS-1$
        }

        @Override
        public Set<String> getConsumedEvents() {
            return consumedEvents;
        }

        @Override
        public void onBrowserEvent(Context context,
                Element parent,
                Boolean value,
                NativeEvent event,
                ValueUpdater<Boolean> valueUpdater) {
            String type = event.getType();
            if ("click".equals(type)) { //$NON-NLS-1$
                // this is required since CheckboxCell does not consume click events
                if (valueUpdater != null) {
                    valueUpdater.update(!value);
                }
            } else {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
            }
        }

        public void renderEditable(Context context, Boolean value, boolean canEdit, SafeHtmlBuilder sb) {
            if (!canEdit) {
                sb.append(value ? INPUT_CHECKED_DISABLED : INPUT_UNCHECKED_DISABLED);
            } else {
                super.render(context, value, sb);
            }
        }

    }

    static class DisabledCheckboxCell extends CheckboxCell {

        @Override
        public void onBrowserEvent(Context context,
                Element parent,
                Boolean value,
                NativeEvent event,
                ValueUpdater<Boolean> valueUpdater) {
            // NOOP
        }

        @Override
        public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
            sb.append(value ? INPUT_CHECKED_DISABLED : INPUT_UNCHECKED_DISABLED);
        }

    }

    /**
     * An html string representation of a checked input box.
     */
    static final SafeHtml INPUT_CHECKED_DISABLED =
            SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled/>"); //$NON-NLS-1$

    /**
     * An html string representation of an unchecked input box.
     */
    static final SafeHtml INPUT_UNCHECKED_DISABLED =
            SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>"); //$NON-NLS-1$

    public EntityModelCheckboxColumn() {
        super(new DisabledCheckboxCell());
    }

    public EntityModelCheckboxColumn(FieldUpdater<EntityModel, Boolean> fieldUpdater) {
        super(new ClickableCheckboxCell());
        setFieldUpdater(fieldUpdater);
    }

    @Override
    public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
        Cell<Boolean> cell = getCell();
        if (cell instanceof ClickableCheckboxCell) {
            ((ClickableCheckboxCell) cell).renderEditable(context, getValue(object), canEdit(object), sb);
        } else {
            super.render(context, object, sb);
        }
    }

    protected abstract boolean canEdit(EntityModel object);

}
