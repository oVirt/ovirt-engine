package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class GetBookmarkByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4325163572694059964L;

    public GetBookmarkByIdParameters(Guid bookmarkId) {
        _bookmarkId = bookmarkId;
    }

    @NotNull(message = "VALIDATION.BOOKMARKS.ID.NOT_NULL")
    private Guid _bookmarkId;

    public Guid getBookmarkId() {
        return _bookmarkId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetBookmarkByIdParameters() {
    }
}
