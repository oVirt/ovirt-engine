package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryUser;
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
        Directory directory = AuthenticationProfileRepository.getInstance().getDirectory(directoryName);
        if (directory == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            DirectoryUser user = directory.findUser(id);
            getQueryReturnValue().setReturnValue(user);
        }
    }

}
