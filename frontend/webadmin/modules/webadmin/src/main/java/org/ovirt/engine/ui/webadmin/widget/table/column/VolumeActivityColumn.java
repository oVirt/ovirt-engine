package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.user.cellview.client.Column;

public class VolumeActivityColumn<T extends GlusterTaskSupport> extends Column<T, GlusterTaskSupport> implements ColumnWithElementId {

    public VolumeActivityColumn(List<HasCell<GlusterTaskSupport, ?>> list) {
        this(new VolumeActivityCompositeCell<GlusterTaskSupport>(list));
    }

    public VolumeActivityColumn(VolumeActivityCompositeCell<GlusterTaskSupport> cell) {
        super(cell);
    }

    @Override
    public GlusterTaskSupport getValue(T object) {
        return object;
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public VolumeActivityCompositeCell getCell() {
        return (VolumeActivityCompositeCell) super.getCell();
    }
}
