package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

public abstract class CheckboxColumn<T> extends Column<T, Boolean> {

    private boolean isCentralized = false;

    static class EnabledDisabledCheckboxCell extends CheckboxCell {

        public EnabledDisabledCheckboxCell() {
            super(true, false);
        }

        public void renderEditable(Context context, Boolean value, boolean canEdit, SafeHtmlBuilder sb) {
            if (!canEdit) {
                sb.append(value ? INPUT_CHECKED_DISABLED : INPUT_UNCHECKED_DISABLED);
            } else {
                super.render(context, value, sb);
            }
        }

    }

    /**
     * An HTML string representation of a checked input box.
     */
    static final SafeHtml INPUT_CHECKED_DISABLED =
            SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled/>"); //$NON-NLS-1$

    /**
     * An HTML string representation of an unchecked input box.
     */
    static final SafeHtml INPUT_UNCHECKED_DISABLED =
            SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>"); //$NON-NLS-1$

    public CheckboxColumn() {
        super(new EnabledDisabledCheckboxCell());
    }

    public CheckboxColumn(boolean isCentralized) {
        super(new EnabledDisabledCheckboxCell());
        this.isCentralized = isCentralized;
    }

    public CheckboxColumn(FieldUpdater<T, Boolean> fieldUpdater) {
        this();
        setFieldUpdater(fieldUpdater);
    }

    @Override
    public void render(Context context, T object, SafeHtmlBuilder sb) {
        Cell<Boolean> cell = getCell();
        if (cell instanceof EnabledDisabledCheckboxCell) {
            if (isCentralized) {
                sb.appendHtmlConstant("<div style='text-align: center'>"); //$NON-NLS-1$
            }

            ((EnabledDisabledCheckboxCell) cell).renderEditable(context, getValue(object), canEdit(object), sb);

            if (isCentralized) {
                sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
            }
        } else {
            super.render(context, object, sb);
        }
    }

    protected abstract boolean canEdit(T object);

}
