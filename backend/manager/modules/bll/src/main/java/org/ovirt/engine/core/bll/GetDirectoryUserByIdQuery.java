package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryManager;
import org.ovirt.engine.core.authentication.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.utils.ExternalId;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String directoryName = getParameters().getDomain();
        ExternalId id = getParameters().getId();
        Directory directory = DirectoryManager.getInstance().getDirectory(directoryName);
        DirectoryUser user = directory.findUser(id);
        getQueryReturnValue().setReturnValue(user);
    }

}
