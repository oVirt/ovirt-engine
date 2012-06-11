package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

public abstract class EntityModelCheckboxColumn extends Column<EntityModel, Boolean> {

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

    public EntityModelCheckboxColumn() {
        super(new EnabledDisabledCheckboxCell());
    }

    public EntityModelCheckboxColumn(FieldUpdater<EntityModel, Boolean> fieldUpdater) {
        this();
        setFieldUpdater(fieldUpdater);
    }

    @Override
    public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
        Cell<Boolean> cell = getCell();
        if (cell instanceof EnabledDisabledCheckboxCell) {
            ((EnabledDisabledCheckboxCell) cell).renderEditable(context, getValue(object), canEdit(object), sb);
        } else {
            super.render(context, object, sb);
        }
    }

    protected abstract boolean canEdit(EntityModel object);

}
