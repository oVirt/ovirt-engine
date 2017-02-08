package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.ui.common.widget.CellClickHandler;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;

import com.google.gwt.event.shared.HandlerRegistration;

public class HotUnplugColumn extends AbstractColumn<VmDevice, VmDevice> implements HasCellClickHandlers<VmDevice> {

    public HotUnplugColumn() {
        super(new HotUnplugCell());
        makeSortable(new Comparator<VmDevice>() {
            @Override
            public int compare(VmDevice device1, VmDevice device2) {
                return toSortingIndex(device1) - toSortingIndex(device2);
            }

            private int toSortingIndex(VmDevice vmDevice) {
                return (vmDevice == null || !HotUnplugCell.isHotUnpluggable(vmDevice))
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
    public HandlerRegistration addHandler(CellClickHandler<VmDevice> handler) {
        return getCell().addHandler(handler);
    }

    @Override
    public VmDevice getValue(VmDevice device) {
        return device;
    }
}
