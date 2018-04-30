package org.ovirt.engine.core.bll.host.provider.foreman;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;

public class FilteredErrataRelativeUrlBuilderTest {

    private static final String HOST_ID = "xxx";
    private static final String CONTENT_HOST_ERRATA_ENDPOINT = "/katello/api/v2/systems/xxx/errata";

    @Test
    public void testUrlWithEmptyFilter() {
        ErrataFilter errataFilter = new ErrataFilter();
        FilteredErrataRelativeUrlBuilder underTest = createFilteredErrataRelativeUrlBuilder(errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata", underTest.build());
    }

    @Test
    public void testUrlWithFilterByTypes() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setErrataTypes(EnumSet.of(Erratum.ErrataType.BUGFIX, Erratum.ErrataType.SECURITY));
        FilteredErrataRelativeUrlBuilder underTest = createFilteredErrataRelativeUrlBuilder(errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?search=type+%3D+bugfix+or+type+%3D+security",
                underTest.build());
    }

    @Test
    public void testUrlWithPagination() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setPageSize(20);
        errataFilter.setPageNumber(3);
        FilteredErrataRelativeUrlBuilder underTest = createFilteredErrataRelativeUrlBuilder(errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?page=3&per_page=20", underTest.build());
    }

    @Test
    public void testUrlWithPaginationAndTypes() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setErrataTypes(EnumSet.of(Erratum.ErrataType.BUGFIX, Erratum.ErrataType.SECURITY));
        errataFilter.setPageSize(20);
        errataFilter.setPageNumber(3);
        FilteredErrataRelativeUrlBuilder underTest = createFilteredErrataRelativeUrlBuilder(errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?search=type+%3D+bugfix+or+type+%3D+security&page=3&per_page=20",
                underTest.build());
    }

    @Test
    public void testUrlWithoutFilter() {
        FilteredErrataRelativeUrlBuilder underTest = createFilteredErrataRelativeUrlBuilder(null);
        assertEquals("/katello/api/v2/systems/xxx/errata", underTest.build());
    }

    private FilteredErrataRelativeUrlBuilder createFilteredErrataRelativeUrlBuilder(ErrataFilter errataFilter) {
        return new FilteredErrataRelativeUrlBuilder(HOST_ID, errataFilter, CONTENT_HOST_ERRATA_ENDPOINT);
    }
}
