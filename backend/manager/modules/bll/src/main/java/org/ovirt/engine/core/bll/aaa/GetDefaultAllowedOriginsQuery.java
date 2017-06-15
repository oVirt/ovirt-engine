package org.ovirt.engine.core.bll.aaa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetDefaultAllowedOriginsQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a list of origins which are allowed by default for CORS (see CORSSupportFilter).
 * Recently it's a list of all hosts.
 *
 * This is required to allow JavaScript/browser calls to REST API, like from Cockpit installed on hosts does.
 *
 * Please note, if either CORSSupport=false or CORSAllowDefaultOrigins=false, then this query is not executed.
 */
public class GetDefaultAllowedOriginsQuery<P extends GetDefaultAllowedOriginsQueryParameters>
        extends QueriesCommandBase<P> {
    private static final Logger log = LoggerFactory.getLogger(GetDefaultAllowedOriginsQuery.class);

    @Inject
    private VdsDao vdsDao;

    public GetDefaultAllowedOriginsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private List<String> getHostOrigins(String host, Set<String> suffixes) {
        List<String> origins = new LinkedList<>();
        suffixes.forEach(suffix -> origins.add(String.format("https://%s%s", host, suffix)));
        return origins;
    }

    private Set<String> getDefaultOrigins() {
        List<VDS> allVds = vdsDao.getAll(getUserID(), getParameters().isFiltered());
        Set<String> allowedOrigins = new HashSet<>();

        if (getParameters().getSuffixes().isEmpty()) {
            allVds.forEach(
                    vds -> allowedOrigins.add(String.format("https://%s", vds.getHostName())));
        } else {
            allVds.forEach(
                    vds -> allowedOrigins.addAll(getHostOrigins(vds.getHostName(), getParameters().getSuffixes())));
        }

        if (log.isDebugEnabled()) {
            log.debug("Default list of origins refreshed to: {}", StringUtils.join(allowedOrigins, ','));
        }
        return allowedOrigins;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDefaultOrigins());
    }
}
