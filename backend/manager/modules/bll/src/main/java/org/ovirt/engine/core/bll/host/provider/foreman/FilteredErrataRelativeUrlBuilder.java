package org.ovirt.engine.core.bll.host.provider.foreman;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.queries.ErrataFilter;
import org.ovirt.engine.core.uutils.net.URLBuilder;

class FilteredErrataRelativeUrlBuilder {

    private static final String DUMMY_ADDRESS_PREFIX = "http://DUMMY_ADDRESS_PREFIX";
    private URLBuilder url;
    private final ErrataFilter errataFilter;

    public FilteredErrataRelativeUrlBuilder(String contentHostId,
            ErrataFilter errataFilter,
            String contentHostEndpoint) {
        this.errataFilter = errataFilter;
        url = new URLBuilder(DUMMY_ADDRESS_PREFIX, String.format(contentHostEndpoint, contentHostId));
    }

    public String build() {
        if (errataFilter != null) {
            if (CollectionUtils.isNotEmpty(errataFilter.getErrataTypes())) {
                List<String> collectedTypes = errataFilter.getErrataTypes()
                        .stream()
                        .map(t -> "type = " + t.getDescription())
                        .collect(Collectors.toList());
                url.addParameter("search", StringUtils.join(collectedTypes, " or "));
            }

            if (errataFilter.getPageNumber() != null) {
                url.addParameter("page", errataFilter.getPageNumber().toString());
            }

            if (errataFilter.getPageSize() != null) {
                url.addParameter("per_page", errataFilter.getPageSize().toString());
            }
        }

        try {
            return url.build().replaceFirst(DUMMY_ADDRESS_PREFIX, "");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static FilteredErrataRelativeUrlBuilder create(String contentHostId,
            ErrataFilter errataFilter,
            String contentHostEndpoint) {
        return new FilteredErrataRelativeUrlBuilder(contentHostId, errataFilter, contentHostEndpoint);
    }
}
