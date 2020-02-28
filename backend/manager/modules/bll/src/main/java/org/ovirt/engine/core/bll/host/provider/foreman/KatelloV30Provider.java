package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class supports the Katello 3.0 API
 */
public class KatelloV30Provider extends KatelloProvider implements ContentHostProvider {
    private static final Logger log = LoggerFactory.getLogger(KatelloV30Provider.class);
    private static final String KATELLO_API_ENTRY_POINT = "/api/v2";
    private static final String CONTENT_HOSTS_ENTRY_POINT = KATELLO_API_ENTRY_POINT + "/hosts";
    private static final String CONTENT_HOST_ERRATA_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata";
    private static final String CONTENT_HOST_ERRATUM_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata/%2$s";

    public KatelloV30Provider(ForemanHostProviderProxy provider) {
        super(provider);
    }

    @Override
    protected String getContentHostId(ContentHostIdentifier contentHostIdentifier) {
        ContentHostV30 contentHost = findContentHost(contentHostIdentifier);
        return contentHost == null ? null : String.valueOf(contentHost.getId());
    }

    @Override
    protected String getContentHostErrataEntryPoint() {
        return CONTENT_HOST_ERRATA_ENTRY_POINT;
    }

    @Override
    protected String getContentHostErratumEntryPoint() {
        return CONTENT_HOST_ERRATUM_ENTRY_POINT;
    }

    @Override
    public boolean isContentHostExist(ContentHostIdentifier contentHostIdentifier) {
        return findContentHost(contentHostIdentifier) != null;
    }

    private ContentHostV30 findContentHost(ContentHostIdentifier contentHostIdentifier) {
        ContentHostV30 host;
        if (StringUtils.isNotBlank(contentHostIdentifier.getId())) {
            host = findContentHostByGivenFact("facts.dmi::system::uuid=" + contentHostIdentifier.getId().toUpperCase());
            if (host != null) {
                return host;
            }
        }

        if (StringUtils.isNotBlank(contentHostIdentifier.getFqdn())) {
            host = findContentHostByGivenFact("facts.network::fqdn=" + contentHostIdentifier.getFqdn());
            if (host != null) {
                return host;
            }
        }

        return findContentHostByGivenFact("facts.network::hostname=" + contentHostIdentifier.getName());
    }

    private ContentHostV30 findContentHostByGivenFact(String fact) {
        final List<ContentHostV30> contentHosts =
                runContentHostListMethod(CONTENT_HOSTS_ENTRY_POINT
                        + String.format(ForemanHostProviderProxy.SEARCH_QUERY_FORMAT, fact));

        if (contentHosts.isEmpty()) {
            return null;
        }

        return contentHosts.get(0);
    }

    private List<ContentHostV30> runContentHostListMethod(String relativeUrl) {
        try {
            ContentHostsV30Wrapper wrapper =
                    objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ContentHostsV30Wrapper.class);
            return Arrays.asList(wrapper.getResults());
        } catch (IOException e) {
            log.error("Failed to parse list of hosts retrieved from provider '{}' with error '{}'",
                    provider.getProvider().getName(),
                    e.getMessage());
            log.debug("Exception: ", e);
            return Collections.emptyList();
        }
    }
}
