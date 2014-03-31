package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String directoryName = getParameters().getDomain();
        String id = getParameters().getId();
        Directory directory = AuthenticationProfileRepository.getInstance().getDirectory(directoryName);
        if (directory == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            DirectoryUser user = directory.findUserById(id);
            getQueryReturnValue().setReturnValue(user);
        }
    }

}
