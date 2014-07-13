package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAAAProfileListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAAAProfileListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<AuthenticationProfile> profiles = AuthenticationProfileRepository.getInstance().getProfiles();
        List<ProfileEntry> names = new ArrayList<>(profiles.size());
        for (AuthenticationProfile profile : profiles) {
            names.add(new ProfileEntry(profile.getName(), AuthzUtils.getName(profile.getAuthz())));
        }
        Collections.sort(names, new Comparator<ProfileEntry>() {

            @Override
            public int compare(ProfileEntry lhs, ProfileEntry rhs) {
                return lhs.getProfile().compareTo(rhs.getProfile()) != 0 ?
                        lhs.getProfile().compareTo(rhs.getProfile())
                        : lhs.getAuthz().compareTo(rhs.getAuthz());

            }
        });
        getQueryReturnValue().setReturnValue(names);
    }

}
