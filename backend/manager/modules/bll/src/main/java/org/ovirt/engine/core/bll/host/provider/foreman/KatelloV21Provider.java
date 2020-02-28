package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class supports the Katello 2.1 API
 */
public class KatelloV21Provider extends KatelloProvider implements ContentHostProvider {
    private static final Logger log = LoggerFactory.getLogger(KatelloV21Provider.class);
    private static final String KATELLO_API_ENTRY_POINT = "/katello/api/v2";
    private static final String CONTENT_HOSTS_ENTRY_POINT = KATELLO_API_ENTRY_POINT + "/systems";
    private static final String CONTENT_HOST_ERRATA_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata";
    private static final String CONTENT_HOST_ERRATUM_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata/%2$s";

    public KatelloV21Provider(ForemanHostProviderProxy provider) {
        super(provider);
    }

    @Override
    protected String getContentHostId(ContentHostIdentifier contentHostIdentifier) {
        ContentHost contentHost = findContentHost(contentHostIdentifier.getName());
        return contentHost == null ? null : contentHost.getUuid();
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
        return findContentHost(contentHostIdentifier.getName()) != null;
    }

    private ContentHost findContentHost(String hostName) {
        final String hostNameFact = "facts.network.hostname:" + hostName;
        final List<ContentHost> contentHosts =
                runContentHostListMethod(CONTENT_HOSTS_ENTRY_POINT
                        + String.format(ForemanHostProviderProxy.SEARCH_QUERY_FORMAT, hostNameFact));

        if (contentHosts.isEmpty()) {
            return null;
        }

        ContentHost latestRegisteredHost = contentHosts.get(0);
        for (int i = 1; i < contentHosts.size(); i++) {
            ContentHost candidateHost = contentHosts.get(i);
            if (candidateHost.getCreated().after(latestRegisteredHost.getCreated())) {
                latestRegisteredHost = candidateHost;
            }
        }

        return latestRegisteredHost;
    }

    private List<ContentHost> runContentHostListMethod(String relativeUrl) {
        try {
            ContentHostsWrapper wrapper =
                    objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ContentHostsWrapper.class);
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
