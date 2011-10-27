package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.*;

public class GetAllNotReadonlyTagsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllNotReadonlyTagsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<tags> returnValue = new java.util.ArrayList<tags>();
        java.util.ArrayList<tags> allTags = TagsDirector.getInstance().GetAllTags();
        for (tags tag : allTags) {
            if (!tag.getIsReadonly()) {
                returnValue.add(tag);
            }
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
