package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

public class GetBookmarkByNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 488190199396867558L;

    public GetBookmarkByNameParameters(String bookmarkName) {
        _bookmarkName = bookmarkName;
    }

    @NotNull(message = "VALIDATION.BOOKMARKS.NAME.NOT_NULL")
    private String _bookmarkName;

    public String getBookmarkName() {
        return _bookmarkName;
    }

    public GetBookmarkByNameParameters() {
    }
}
