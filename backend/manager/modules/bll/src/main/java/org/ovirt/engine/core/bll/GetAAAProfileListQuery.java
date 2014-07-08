package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAAAProfileListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAAAProfileListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<AuthenticationProfile> profiles = AuthenticationProfileRepository.getInstance().getProfiles();
        List<String> names = new ArrayList<>(profiles.size());
        for (AuthenticationProfile profile : profiles) {
            names.add(profile.getName());
        }
        Collections.sort(names);

        getQueryReturnValue().setReturnValue(names);
    }

}
