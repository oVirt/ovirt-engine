package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryManager;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.utils.ExternalId;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String directoryName = getParameters().getDomain();
        final ExternalId id = getParameters().getId();
        final Directory directory = DirectoryManager.getInstance().getDirectory(directoryName);
        if (directory == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            final DirectoryGroup group = directory.findGroup(id);
            getQueryReturnValue().setReturnValue(group);
        }
    }

}
