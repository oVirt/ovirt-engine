package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public abstract class AbstractCheckboxHeader extends AbstractHeader<Boolean> {

    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\" tabindex=\"-1\" checked />"); //$NON-NLS-1$

    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\" />"); //$NON-NLS-1$

    public AbstractCheckboxHeader() {
        super(new CheckboxCell(true, false) {

            // Override this to add MOUSEMOVE for mouse cursor changes.
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<>(super.getConsumedEvents());
                set.add(BrowserEvents.MOUSEMOVE);
                return set;
            }

            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
                super.render(context, value, sb, id);
            }

        });

        setUpdater(value -> selectionChanged(value));

        if (getTooltip() != null) {
            getCell().setTooltipFallback(getTooltip());
        }

        if (getLabel() != null) {
            ((CheckboxCell) getCell()).setLabel(SafeHtmlUtils.fromString(getLabel()));
        }
    }

    /**
     * Override to set the value of the label.
     * @return A string representing the label, or null if not defined.
     */
    public String getLabel() {
        return null;
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
        if (!isEnabled()) {
            if (getValue()) {
                sb.append(INPUT_CHECKED_DISABLED);
            } else {
                sb.append(INPUT_UNCHECKED_DISABLED);
            }
            if (getLabel() != null && StringHelper.isNotNullOrEmpty(getLabel())) {
                sb.append(SafeHtmlUtils.fromString(getLabel()));
            }
        } else {
            super.render(context, sb);
        }
    }

    protected abstract void selectionChanged(Boolean value);

    public abstract boolean isEnabled();

    @Override
    public SafeHtml getTooltip() {
        return SafeHtmlUtils.EMPTY_SAFE_HTML;
    };

}
