package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String directoryName = getParameters().getDomain();
        final String id = getParameters().getId();
        final Directory directory = AuthenticationProfileRepository.getInstance().getDirectory(directoryName);
        if (directory == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            final DirectoryGroup group = directory.findGroupById(id);
            getQueryReturnValue().setReturnValue(group);
        }
    }

}
