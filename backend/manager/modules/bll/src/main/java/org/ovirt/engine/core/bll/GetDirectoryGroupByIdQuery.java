package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryGroup;
import org.ovirt.engine.core.authentication.DirectoryManager;
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
        final DirectoryGroup group = directory.findGroup(id);
        getQueryReturnValue().setReturnValue(group);
    }

}
