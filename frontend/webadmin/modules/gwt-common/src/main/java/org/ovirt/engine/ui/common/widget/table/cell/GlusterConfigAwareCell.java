package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.CellPreviewEvent;

public class GlusterConfigAwareCell extends AbstractCell<GlusterGeoRepSessionConfiguration> implements EventHandlingCell {

    private SelectionCell delegate;
    private TextInputCell textInputCell;

    public GlusterConfigAwareCell() {
        super(BrowserEvents.CHANGE);
        delegate = new SelectionCell(new ArrayList<String>());
        textInputCell = new TextInputCell();
    }

    private void setOptions(List<String> allowedValues) {
        delegate = new SelectionCell(allowedValues);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return BrowserEvents.CLICK.equals(event.getNativeEvent().getType());
    }

    @Override
    public void onBrowserEvent(Context context, final Element parent, final GlusterGeoRepSessionConfiguration configInRow, NativeEvent event, ValueUpdater<GlusterGeoRepSessionConfiguration> valueUpdater) {
        final List<String> allowedValuesList = configInRow.getAllowedValues();
        boolean isValuesConstrained =
                isValueConstrained(allowedValuesList);
        if (isValuesConstrained) {
            delegate.onBrowserEvent(context, parent, configInRow.getValue(), event, value -> {
                SelectElement select = parent.getFirstChild().cast();
                int selectedIndex = select.getSelectedIndex();
                configInRow.setValue(allowedValuesList.get(selectedIndex));
            });
        } else {
            textInputCell.onBrowserEvent(context, parent, configInRow.getValue(), event, value -> {
                if (value != null) {
                    configInRow.setValue(value);
                }

            });
        }

    }

    private boolean isValueConstrained(List<String> allowedValuesList) {
        return allowedValuesList != null && !allowedValuesList.isEmpty()
                && !(allowedValuesList.size() == 1 && allowedValuesList.get(0).isEmpty());
    }

    @Override
    public void render(Context context, GlusterGeoRepSessionConfiguration value, SafeHtmlBuilder sb) {
        List<String> allowedValues = value.getAllowedValues();
        boolean isValuesConstrained = isValueConstrained(allowedValues);
        SafeHtmlBuilder sbDelegate = new SafeHtmlBuilder();
        if (isValuesConstrained) {
            setOptions(allowedValues);
            delegate.render(context, value.getDefaultValue(), sbDelegate);
        } else {
            textInputCell.render(context, value.getDefaultValue(), sbDelegate);
        }
        sb.append(sbDelegate.toSafeHtml());
    }

}
