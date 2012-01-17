package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetBookmarkByIdParameters")
public class GetBookmarkByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4325163572694059964L;

    public GetBookmarkByIdParameters(Guid bookmarkId) {
        _bookmarkId = bookmarkId;
    }

    @NotNull(message = "VALIDATION.BOOKMARKS.ID.NOT_NULL")
    @XmlElement(name = "BookmarkId")
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
