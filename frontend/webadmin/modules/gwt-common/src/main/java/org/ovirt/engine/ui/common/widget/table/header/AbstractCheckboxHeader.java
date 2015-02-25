package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public abstract class AbstractCheckboxHeader extends AbstractHeader<Boolean> {

    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\" tabindex=\"-1\" checked/>"); //$NON-NLS-1$

    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant(
            "<input type=\"checkbox\" disabled=\"\" tabindex=\"-1\"/>"); //$NON-NLS-1$

    public AbstractCheckboxHeader() {
        super(new CheckboxCell(true, false) {

            @Override
            public Set<String> getConsumedEvents() {  // override this to add MOUSEMOVE for mouse cursor changes
                Set<String> set = new HashSet<String>();
                TooltipMixin.addTooltipsEvents(set);
                set.add(BrowserEvents.CHANGE);
                set.add(BrowserEvents.KEYDOWN);
                set.add(BrowserEvents.MOUSEMOVE);
                return set;
            }

            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
                super.render(context, value, sb, id);
            }
        });

        setUpdater(new ValueUpdater<Boolean>() {
            @Override
            public void update(Boolean value) {
                selectionChanged(value);
            }
        });
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
        if (!isEnabled()) {
            if (getValue()) {
                sb.append(INPUT_CHECKED_DISABLED);
            } else {
                sb.append(INPUT_UNCHECKED_DISABLED);
            }
        } else {
            super.render(context, sb);
        }
    }

    abstract protected void selectionChanged(Boolean value);

    abstract public boolean isEnabled();

    @Override
    public abstract SafeHtml getTooltip();

}
