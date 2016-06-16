package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.ovirt.engine.core.common.businessentities.ErrataCount;
import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class supports the Katello 2.1 API
 */
public class KatelloV21Provider implements ContentHostProvider {
    private static final Logger log = LoggerFactory.getLogger(KatelloV21Provider.class);
    private static final String KATELLO_API_ENTRY_POINT = "/katello/api/v2";
    private static final String CONTENT_HOSTS_ENTRY_POINT = KATELLO_API_ENTRY_POINT + "/systems";
    static final String CONTENT_HOST_ERRATA_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata";
    private static final String CONTENT_HOST_ERRATUM_ENTRY_POINT = CONTENT_HOSTS_ENTRY_POINT + "/%1$s/errata/%2$s";
    private static final Integer UNLIMITED_PAGE_SIZE = 999999;
    private ForemanHostProviderProxy provider;
    private ObjectMapper objectMapper;

    public KatelloV21Provider(ForemanHostProviderProxy provider) {
        this.provider = provider;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ContentHost findContentHost(String hostName) {
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
            return Collections.emptyList();
        }
    }

    private ErrataData runErrataListMethod(String relativeUrl, String hostName) {
        ErrataData errataData = new ErrataData();

        try {
            ErrataWrapper wrapper = objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ErrataWrapper.class);
            errataData.setErrata(mapErrata(Arrays.asList(wrapper.getResults())));
            errataData.setErrataCounts(mapErrataCounts(wrapper));
            Stream.of(Erratum.ErrataType.values()).forEach(errataType -> addErrataCountForType(errataData, errataType));
        } catch (Exception e) {
            log.error("Failed to retrieve errata for content host '{}' via url '{}': {}",
                    hostName,
                    relativeUrl,
                    e.getMessage());
            log.debug("Exception", e);
            return ErrataData.emptyData();
        }

        return errataData;
    }

    private void addErrataCountForType(ErrataData errataData, Erratum.ErrataType errataType) {
        Stream<Erratum> typedErrata =
                errataData.getErrata().stream().filter(erratum -> erratum.getType() == errataType);
        long totalCount = typedErrata.count();
        if (totalCount > 0) {
            Map<Erratum.ErrataSeverity, Long> errataBySeverity =
                    errataData.getErrata().stream().collect(
                            Collectors.groupingBy(Erratum::getSeverityOrDefault, Collectors.counting()));

            ErrataCount errataCount = new ErrataCount();
            errataCount.setTotalCount((int) totalCount);
            errataBySeverity.entrySet()
                    .stream()
                    .forEach(entry -> errataCount.getCountBySeverity().put(entry.getKey(),
                            entry.getValue().intValue()));

            errataData.getErrataCounts().getErrataCountByType().put(errataType, errataCount);
        }
    }

    private ErrataCounts mapErrataCounts(ErrataWrapper wrapper) {
        ErrataCounts errataCounts = new ErrataCounts();
        errataCounts.setTotalErrata(wrapper.getTotalCount());
        errataCounts.setSubTotalErrata(wrapper.getSubTotalCount());
        return errataCounts;
    }

    private List<Erratum> mapErrata(List<ExternalErratum> externalErrata) {
        List<Erratum> errata = new ArrayList<>(externalErrata.size());
        for (ExternalErratum externalErratum : externalErrata) {
            Erratum erratum = mapErratum(externalErratum);
            errata.add(erratum);
        }

        return errata;
    }

    private Erratum mapErratum(ExternalErratum externalErratum) {
        Erratum erratum = new Erratum();
        erratum.setId(externalErratum.getId());
        erratum.setIssued(externalErratum.getIssued());
        erratum.setTitle(externalErratum.getTitle());
        erratum.setSummary(externalErratum.getSummary());
        erratum.setSolution(externalErratum.getSolution());
        erratum.setDescription(externalErratum.getDescription());
        erratum.setSeverity(Erratum.ErrataSeverity.byDescription(externalErratum.getSeverity()));
        erratum.setType(Erratum.ErrataType.byDescription(externalErratum.getType()));
        erratum.setPackages(Arrays.asList(externalErratum.getPackages()));
        return erratum;
    }

    @Override
    public Erratum getErratumForHost(String hostName, String erratumId) {
        ContentHost contentHost = findContentHost(hostName);
        if (contentHost == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    hostName);
            return null;
        }

        return runErratumMethod(String.format(CONTENT_HOST_ERRATUM_ENTRY_POINT, contentHost.getUuid(), erratumId));
    }

    @Override
    public ErrataData getErrataForHost(String hostName, ErrataFilter errataFilter) {
        ContentHost contentHost = findContentHost(hostName);
        if (contentHost == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    hostName);
            return ErrataData.emptyData();
        }

        if (errataFilter == null) {
            errataFilter = new ErrataFilter();
            errataFilter.setErrataTypes(EnumSet.allOf(Erratum.ErrataType.class));
        }

        // For calculating the errata counts there is a need to fetch all of the errata information
        errataFilter.setPageSize(UNLIMITED_PAGE_SIZE);
        String relativeUrl = FilteredErrataRelativeUrlBuilder.create(contentHost.getUuid(), errataFilter).build();
        return runErrataListMethod(relativeUrl, hostName);
    }

    private Erratum runErratumMethod(String relativeUrl) {
        try {
            ExternalErratum erratum =
                    objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ExternalErratum.class);
            return mapErratum(erratum);
        } catch (IOException e) {
            return null;
        }
    }
}
