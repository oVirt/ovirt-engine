package org.ovirt.engine.ui.common.widget.table.header;

import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;

public abstract class CheckboxHeader extends Header<Boolean> {

    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\" tabindex=\"-1\" checked/>"); //$NON-NLS-1$

    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\"/>"); //$NON-NLS-1$

    private final SafeHtml title;

    public SafeHtml getTitle() {
        return title;
    }

    public CheckboxHeader(final SafeHtml title) {
        super(new CheckboxCell(true, false) {
            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
                super.render(context, value, sb, id);
                sb.append(title);
            }
        });

        setUpdater(new ValueUpdater<Boolean>() {
            @Override
            public void update(Boolean value) {
                selectionChanged(value);
            }
        });

        this.title = title;
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
        if (!isEnabled()) {
            if (getValue()) {
                sb.append(INPUT_CHECKED_DISABLED);
                sb.append(title);
            } else {
                sb.append(INPUT_UNCHECKED_DISABLED);
                sb.append(title);
            }
        } else {
            super.render(context, sb);
        }
    }

    abstract protected void selectionChanged(Boolean value);

    abstract public boolean isEnabled();

}
