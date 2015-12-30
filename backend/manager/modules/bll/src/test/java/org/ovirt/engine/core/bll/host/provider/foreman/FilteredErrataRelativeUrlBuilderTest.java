package org.ovirt.engine.core.bll.host.provider.foreman;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;

public class FilteredErrataRelativeUrlBuilderTest {

    private static final String HOST_ID = "xxx";

    @Test
    public void testUrlWithEmptyFilter() {
        ErrataFilter errataFilter = new ErrataFilter();
        FilteredErrataRelativeUrlBuilder underTest = new FilteredErrataRelativeUrlBuilder(HOST_ID, errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata", underTest.build());
    }

    @Test
    public void testUrlWithFilterByTypes() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setErrataTypes(EnumSet.of(Erratum.ErrataType.BUGFIX, Erratum.ErrataType.SECURITY));
        FilteredErrataRelativeUrlBuilder underTest = new FilteredErrataRelativeUrlBuilder(HOST_ID, errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?search=type+%3D+bugfix+or+type+%3D+security",
                underTest.build());
    }

    @Test
    public void testUrlWithPagination() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setPageSize(20);
        errataFilter.setPageNumber(3);
        FilteredErrataRelativeUrlBuilder underTest = new FilteredErrataRelativeUrlBuilder(HOST_ID, errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?page=3&per_page=20", underTest.build());
    }

    @Test
    public void testUrlWithPaginationAndTypes() {
        ErrataFilter errataFilter = new ErrataFilter();
        errataFilter.setErrataTypes(EnumSet.of(Erratum.ErrataType.BUGFIX, Erratum.ErrataType.SECURITY));
        errataFilter.setPageSize(20);
        errataFilter.setPageNumber(3);
        FilteredErrataRelativeUrlBuilder underTest = new FilteredErrataRelativeUrlBuilder(HOST_ID, errataFilter);
        assertEquals("/katello/api/v2/systems/xxx/errata?search=type+%3D+bugfix+or+type+%3D+security&page=3&per_page=20",
                underTest.build());
    }

    @Test
    public void testUrlWithoutFilter() {
        FilteredErrataRelativeUrlBuilder underTest = new FilteredErrataRelativeUrlBuilder(HOST_ID, null);
        assertEquals("/katello/api/v2/systems/xxx/errata", underTest.build());
    }
}
