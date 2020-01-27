package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.compat.Guid;

public class IdAndChipsetQueryParameters extends IdQueryParameters {

    private static final long serialVersionUID = 5914607088348386558L;
    private ChipsetType chipsetType;

    @SuppressWarnings("unused")
    private IdAndChipsetQueryParameters() {
    }

    public IdAndChipsetQueryParameters(Guid id, ChipsetType chipsetType) {
        super(id);
        this.chipsetType = chipsetType;
    }

    public ChipsetType getChipsetType() {
        return chipsetType;
    }

    public void setChipsetType(ChipsetType chipsetType) {
        this.chipsetType = chipsetType;
    }

}
