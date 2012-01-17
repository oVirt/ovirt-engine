package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetBookmarkByNameParameters")
public class GetBookmarkByNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 488190199396867558L;

    public GetBookmarkByNameParameters(String bookmarkName) {
        _bookmarkName = bookmarkName;
    }

    @NotNull(message = "VALIDATION.BOOKMARKS.NAME.NOT_NULL")
    @XmlElement(name = "BookmarkName")
    private String _bookmarkName;

    public String getBookmarkName() {
        return _bookmarkName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetBookmarkByNameParameters() {
    }
}
