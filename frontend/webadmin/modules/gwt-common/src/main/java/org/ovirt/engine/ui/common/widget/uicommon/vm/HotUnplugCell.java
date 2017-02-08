package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.CellClickHandler;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class HotUnplugCell extends AbstractCell<VmDevice> implements HasCellClickHandlers<VmDevice> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<button class=\"btn btn-default\" id=\"{0}\">{1}</button>")
        SafeHtml button(String id, String hotUnplugLabel);
    }

    private static CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    private Set<CellClickHandler<VmDevice>> clickHandlers = new HashSet<>();

    @Override
    public HandlerRegistration addHandler(final CellClickHandler<VmDevice> handler) {
        clickHandlers.add(handler);
        return new HandlerRegistration() {
            @Override public void removeHandler() {
                clickHandlers.remove(handler);
            }
        };
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            VmDevice device,
            NativeEvent event,
            ValueUpdater<VmDevice> valueUpdater) {
        super.onBrowserEvent(context, parent, device, event, valueUpdater);
        if (!BrowserEvents.CLICK.equals(event.getType())) {
            return;
        }
        if (parent.getFirstChildElement() == null
                || !parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
            return;
        }
        for (CellClickHandler<VmDevice> clickHandler : clickHandlers) {
            clickHandler.onClick(event, device);
        }
    }

    @Override
    public void render(Context context, VmDevice device, SafeHtmlBuilder sb, String id) {
        if (isHotUnpluggable(device)) {
            return;
        }
        sb.append(cellTemplate.button(id, AssetProvider.getConstants().hotUnplug()));
    }

    public static boolean isHotUnpluggable(VmDevice vmDevice) {
        return vmDevice.getType() != VmDeviceGeneralType.MEMORY;
    }

    @Override
    public Set<String> getConsumedEvents() {
        final Set<String> consumedEvents = new HashSet<>(super.getConsumedEvents());
        consumedEvents.add(BrowserEvents.CLICK);
        return consumedEvents;
    }
}
