package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.vdscommands.PostDeleteAction;
import org.ovirt.engine.core.common.vdscommands.StorageDomainIdParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class PostDeleteActionHandler {

    private PostDeleteActionHandler(){}

    /**
     * Since the file system is responsible for handling block allocation, there is no need
     * for posting zeros on file domains. This method gets the parameters of a command that may
     * post zeros on the storage and fixes its postZero value if required.
     * @param parameters The parameters of the command that should be executed.
     * @param <T> The parameters type.
     * @return The fixed parameters.
     */
    public static <T extends StorageDomainIdParametersBase & PostDeleteAction> T fixParameters(T parameters) {
        StorageDomainStatic storageDomainStatic =
                DbFacade.getInstance().getStorageDomainStaticDao().get(parameters.getStorageDomainId());
        return fixParameters(parameters, storageDomainStatic.getStorageType().isFileDomain());
    }

    protected static <T extends StorageDomainIdParametersBase & PostDeleteAction>
                    T fixParameters(T parameters, boolean isFileDomain) {
        if (isFileDomain) {
            parameters.setPostZero(false);
        }
        return parameters;
    }

}
