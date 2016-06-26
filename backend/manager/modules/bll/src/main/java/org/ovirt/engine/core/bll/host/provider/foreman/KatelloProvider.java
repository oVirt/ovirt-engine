package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for Katello providers
 */
public abstract class KatelloProvider implements ContentHostProvider {

    private static final Logger log = LoggerFactory.getLogger(KatelloProvider.class);
    protected ForemanHostProviderProxy provider;
    protected ObjectMapper objectMapper;

    public KatelloProvider(ForemanHostProviderProxy provider) {
        this.provider = provider;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Erratum getErratumForHost(String hostName, String erratumId) {
        String contentHostId = getContentHostId(hostName);
        if (contentHostId == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    hostName);
            return null;
        }

        return runErratumMethod(String.format(getContentHostErratumEntryPoint(), contentHostId, erratumId));
    }

    @Override
    public List<Erratum> getErrataForHost(String hostName) {
        String contentHostId = getContentHostId(hostName);
        if (contentHostId == null) {
            log.error("Failed to find host on provider '{}' by host name '{}' ",
                    provider.getProvider().getName(),
                    hostName);
            return Collections.emptyList();
        }

        return runErrataListMethod(String.format(getContentHostErrataEntryPoint(), contentHostId));
    }

    private List<Erratum> runErrataListMethod(String relativeUrl) {
        try {
            ErrataWrapper wrapper = objectMapper.readValue(provider.runHttpGetMethod(relativeUrl), ErrataWrapper.class);
            return mapErrata(Arrays.asList(wrapper.getResults()));
        } catch (IOException e) {
            return null;
        }
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

    protected abstract String getContentHostId(String hostName);

    protected abstract String getContentHostErrataEntryPoint();

    protected abstract String getContentHostErratumEntryPoint();

    private List<Erratum> mapErrata(List<ExternalErratum> externalErrata) {
        List<Erratum> errata = new ArrayList<>(externalErrata.size());
        for (ExternalErratum externalErratum : externalErrata) {
            Erratum erratum = mapErratum(externalErratum);
            errata.add(erratum);
        }

        return errata;
    }

}
