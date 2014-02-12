package org.ovirt.engine.core.bll;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileManager;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;

public class GetDomainListQuery<P extends GetDomainListParameters> extends QueriesCommandBase<P> {
    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Get the name of the internal domain:
        String internal = Config.<String> getValue(ConfigValues.AdminDomain);

        // Get the list of authentication profile names:
        List<AuthenticationProfile> profiles = AuthenticationProfileManager.getInstance().getProfiles();
        List<String> names = new ArrayList<>(profiles.size());
        for (AuthenticationProfile profile : profiles) {
            names.add(profile.getName());
        }
        if (getParameters().getFilterInternalDomain()) {
            names.remove(internal);
        }

        // Sort it so that the internal profile is always the last:
        sort(names, new LoginDomainComparator(internal));

        // Return the sorted list:
        getQueryReturnValue().setReturnValue(names);
    }
}
