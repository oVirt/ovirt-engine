package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.Comparator;

import org.ovirt.engine.ui.common.widget.CellClickHandler;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;

import com.google.gwt.event.shared.HandlerRegistration;

public class HotUnplugColumn extends AbstractColumn<VmDeviceFeEntity, VmDeviceFeEntity> implements HasCellClickHandlers<VmDeviceFeEntity> {

    public HotUnplugColumn() {
        super(new HotUnplugCell());
        makeSortable(new Comparator<VmDeviceFeEntity>() {
            @Override
            public int compare(VmDeviceFeEntity device1, VmDeviceFeEntity device2) {
                return toSortingIndex(device1) - toSortingIndex(device2);
            }

            private int toSortingIndex(VmDeviceFeEntity vmDevice) {
                return (vmDevice == null || !HotUnplugCell.isHotUnpluggable(vmDevice.getVmDevice()))
                        ? 0
                        : 1;
            }
        });
    }

    @Override
    public HotUnplugCell getCell() {
        return (HotUnplugCell) super.getCell();
    }

    @Override
    public HandlerRegistration addHandler(CellClickHandler<VmDeviceFeEntity> handler) {
        return getCell().addHandler(handler);
    }

    @Override
    public VmDeviceFeEntity getValue(VmDeviceFeEntity device) {
        return device;
    }
}
