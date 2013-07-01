package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * A {@link TextCellWithTooltip} whose text may be edited.
 */
public class EditTextCellWithTooltip extends TextCellWithTooltip implements EventHandlingCell {

    private EditTextCell delegate;

    public EditTextCellWithTooltip(int maxTextLength) {
        super(maxTextLength, "mouseover", "click", "keyup", "keydown", "blur"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$ $NON-NLS-5$
        delegate = new EditTextCell();
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        delegate.render(context, value, sb);
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            String value,
            NativeEvent event,
            ValueUpdater<String> valueUpdater) {

        delegate.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return true;
    }

}
