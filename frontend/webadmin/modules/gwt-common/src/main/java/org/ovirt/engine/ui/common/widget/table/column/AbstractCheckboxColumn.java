package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Column that renders a checkbox. Supports tooltips.
 */
public abstract class AbstractCheckboxColumn<T> extends AbstractColumn<T, Boolean> {

    private boolean centered = false;

    private boolean multipleSelectionAllowed = true;

    private static final SafeHtml INPUT_CHECKBOX_DISABLED_PREFIX =
            SafeHtmlUtils.fromTrustedString("<input type=\"checkbox\" tabindex=\"-1\" disabled"); //$NON-NLS-1$
    private static final SafeHtml INPUT_RADIO_DISABLED_PREFIX =
            SafeHtmlUtils.fromTrustedString("<input type=\"radio\" tabindex=\"-1\" disabled"); //$NON-NLS-1$
    private static final SafeHtml CHECKED_ATTR = SafeHtmlUtils.fromTrustedString(" checked"); //$NON-NLS-1$
    private static final SafeHtml TAG_CLOSE = SafeHtmlUtils.fromTrustedString("/>"); //$NON-NLS-1$

    public AbstractCheckboxColumn() {
        super(new CheckboxCell(true, false));
    }

    public AbstractCheckboxColumn(boolean centered) {
        super(new CheckboxCell(true, false));
        this.centered = centered;
    }

    public AbstractCheckboxColumn(FieldUpdater<T, Boolean> fieldUpdater) {
        this();
        setFieldUpdater(fieldUpdater);
    }

    public AbstractCheckboxColumn(boolean multipleSelectionAllowed, FieldUpdater<T, Boolean> fieldUpdater) {
        super(multipleSelectionAllowed ? new CheckboxCell(true, false) : new RadioboxCell(true, false));
        this.multipleSelectionAllowed = multipleSelectionAllowed;
        setFieldUpdater(fieldUpdater);
    }

    @Override
    public void render(Context context, T object, SafeHtmlBuilder sb) {
        if (centered) {
            sb.appendHtmlConstant("<div style='text-align: center'>"); //$NON-NLS-1$
        }

        if (!canEdit(object)) {
            sb.append(multipleSelectionAllowed ? INPUT_CHECKBOX_DISABLED_PREFIX : INPUT_RADIO_DISABLED_PREFIX);
            if (Boolean.TRUE.equals(getValue(object))) {
                sb.append(CHECKED_ATTR);
            }
            sb.append(TAG_CLOSE);
        } else {
            // call the Cell render
            super.render(context, object, sb);
        }

        if (centered) {
            sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
        }
    }

    protected abstract boolean canEdit(T object);

    protected String getDisabledMessage(T object) {
        return null;
    }

    /**
     * Enables default <em>client-side</em> sorting for this column, by displaying values of false before true.
     */
    public void makeSortable() {
        makeSortable(Comparator.comparing(t -> !(getValue(t) == null ? false : getValue(t))));
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public SafeHtml getTooltip(T object) {
        if (!canEdit(object)) {
            String disabledMessage = getDisabledMessage(object);
            if (disabledMessage != null && !disabledMessage.isEmpty()) {
                return SafeHtmlUtils.fromString(disabledMessage);
            }
        }
        return null;
    }

    protected boolean isMultipleSelectionAllowed() {
        return multipleSelectionAllowed;
    }
}
