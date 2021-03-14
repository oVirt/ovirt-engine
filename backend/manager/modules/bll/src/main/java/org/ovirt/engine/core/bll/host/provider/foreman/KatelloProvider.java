package org.ovirt.engine.core.bll.host.provider.foreman;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.ovirt.engine.core.common.businessentities.ErrataCount;
import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A base class for Katello providers
 */
public abstract class KatelloProvider  implements ContentHostProvider {

    private static final Logger log = LoggerFactory.getLogger(KatelloProvider.class);
    protected static final Integer UNLIMITED_PAGE_SIZE = 999999;
    protected ForemanHostProviderProxy provider;
    protected ObjectMapper objectMapper;

    public KatelloProvider(ForemanHostProviderProxy provider) {
        this.provider = provider;
        objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Erratum getErratumForHost(ContentHostIdentifier contentHostIdentifier, String erratumId) {
        String contentHostId = getContentHostId(contentHostIdentifier);
        if (contentHostId == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    contentHostIdentifier.getName());
            return null;
        }

        return runErratumMethod(String.format(getContentHostErratumEntryPoint(), contentHostId, erratumId));
    }

    @Override
    public ErrataData getErrataForHost(ContentHostIdentifier contentHostIdentifier, ErrataFilter errataFilter) {
        String contentHostId = getContentHostId(contentHostIdentifier);
        if (contentHostId == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    contentHostIdentifier.getName());
            return ErrataData.emptyData();
        }

        if (errataFilter == null) {
            errataFilter = new ErrataFilter();
            errataFilter.setErrataTypes(EnumSet.allOf(Erratum.ErrataType.class));
        }

        // For calculating the errata counts there is a need to fetch all of the errata information
        errataFilter.setPageSize(UNLIMITED_PAGE_SIZE);
        String relativeUrl =
                FilteredErrataRelativeUrlBuilder.create(contentHostId, errataFilter, getContentHostErrataEntryPoint())
                        .build();
        return runErrataListMethod(relativeUrl, contentHostIdentifier.getName());
    }

    protected ErrataData runErrataListMethod(String relativeUrl, String hostName) {
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

    protected Erratum mapErratum(ExternalErratum externalErratum) {
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

    protected Erratum runErratumMethod(String relativeUrl) {
        try {
            ExternalErratum erratum =
                    objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ExternalErratum.class);
            return mapErratum(erratum);
        } catch (IOException e) {
            return null;
        }
    }

    protected abstract String getContentHostId(ContentHostIdentifier contentHostIdentifier);

    protected abstract String getContentHostErrataEntryPoint();

    protected abstract String getContentHostErratumEntryPoint();

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

}
