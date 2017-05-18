package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.CellClickHandler;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class HotUnplugCell extends AbstractCell<VmDeviceFeEntity>
        implements HasCellClickHandlers<VmDeviceFeEntity> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<button class=\"btn btn-default\" id=\"{0}\">{1}</button>")
        SafeHtml button(String id, String hotUnplugLabel);

        @Template("<button class=\"btn btn-default\" id=\"{0}\" disabled title=\"{2}\">{1}</button>")
        SafeHtml disabledButton(String id, String hotUnplugLabel, String title);
    }

    private static CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    private Set<CellClickHandler<VmDeviceFeEntity>> clickHandlers = new HashSet<>();

    @Override
    public HandlerRegistration addHandler(final CellClickHandler<VmDeviceFeEntity> handler) {
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
            VmDeviceFeEntity device,
            NativeEvent event,
            ValueUpdater<VmDeviceFeEntity> valueUpdater) {
        super.onBrowserEvent(context, parent, device, event, valueUpdater);
        if (!BrowserEvents.CLICK.equals(event.getType())) {
            return;
        }
        if (parent.getFirstChildElement() == null
                || !parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
            return;
        }
        final boolean isButtonEnabled = device.getVm().getStatus() == VMStatus.Up && !device.isBeingUnplugged();
        if (!isButtonEnabled) {
            return;
        }
        for (CellClickHandler<VmDeviceFeEntity> clickHandler : clickHandlers) {
            clickHandler.onClick(event, device);
        }
    }

    @Override
    public void render(Context context, VmDeviceFeEntity deviceEntity, SafeHtmlBuilder sb, String id) {
        if (!isHotUnpluggable(deviceEntity.getVmDevice())) {
            return;
        }
        if (deviceEntity.getVm() == null) {
            return;
        }
        final boolean memoryHotUnplugSupported =
                AsyncDataProvider.getInstance().isMemoryHotUnplugSupportedByArchitecture(
                        deviceEntity.getVm().getClusterArch(),
                        deviceEntity.getVm().getCompatibilityVersion());
        if (!memoryHotUnplugSupported) {
            sb.append(cellTemplate.disabledButton(
                    id,
                    AssetProvider.getConstants().hotUnplug(),
                    AssetProvider.getMessages().memoryHotUnplugNotSupportedForCompatibilityVersionAndArchitecture(
                            deviceEntity.getVm().getCompatibilityVersion(),
                            deviceEntity.getVm().getClusterArch())));
            return;
        }
        if (deviceEntity.getVm().getStatus() != VMStatus.Up) {
            sb.append(cellTemplate.disabledButton(
                    id, AssetProvider.getConstants().hotUnplug(), AssetProvider.getConstants().vmHasToBeUp()));
            return;
        }
        if (deviceEntity.isBeingUnplugged()) {
            sb.append(cellTemplate.disabledButton(id, AssetProvider.getConstants().unplugging(), ""));
            return;
        }
        // This `if` branch can be removed together with support of snapshots created in 4.1, see BZ#1452631
        if (specParamsMissing(deviceEntity.getVmDevice())) {
            sb.append(cellTemplate.disabledButton(id, AssetProvider.getConstants().hotUnplug(),
                    AssetProvider.getConstants().deviceCantBeHotUnplugged()));
            return;
        }
        sb.append(cellTemplate.button(id, AssetProvider.getConstants().hotUnplug()));
    }

    private boolean specParamsMissing(VmDevice vmDevice) {
        return !VmDeviceCommonUtils.getSpecParamsIntValue(vmDevice, VmDeviceCommonUtils.SPEC_PARAM_SIZE).isPresent()
                || !VmDeviceCommonUtils.getSpecParamsIntValue(vmDevice, VmDeviceCommonUtils.SPEC_PARAM_NODE).isPresent();
    }

    public static boolean isHotUnpluggable(VmDevice vmDevice) {
        return vmDevice.getType() == VmDeviceGeneralType.MEMORY;
    }

    @Override
    public Set<String> getConsumedEvents() {
        final Set<String> consumedEvents = new HashSet<>(super.getConsumedEvents());
        consumedEvents.add(BrowserEvents.CLICK);
        return consumedEvents;
    }
}
