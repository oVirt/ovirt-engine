package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetAAAProfileListQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    public GetAAAProfileListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = SsoOAuthServiceUtils.getProfileList();
        List<ProfileEntry> names = new ArrayList<>();
        if (response.containsKey("result")) {
            names = ((List<Map<String, Object>>) response.get("result")).stream()
                    .map(this::mapToProfileEntry)
                    .collect(Collectors.toList());
        }

        Collections.sort(names, (lhs, rhs) ->
                lhs.getProfile().compareTo(rhs.getProfile()) != 0 ?
                        lhs.getProfile().compareTo(rhs.getProfile())
                        : lhs.getAuthz().compareTo(rhs.getAuthz())

        );
        getQueryReturnValue().setReturnValue(names);
    }

    private ProfileEntry mapToProfileEntry(Map<String, Object> profileInfo) {
        return new ProfileEntry((String) profileInfo.get("authn_name"),
                (String) profileInfo.get("authz_name"),
                (boolean) profileInfo.get("capability_password_auth"));
    }

}
