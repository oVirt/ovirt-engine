package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.STATISTICS;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.getPrototype;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.VDS;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStatisticResourceTest extends AbstractBackendSubResourceTest<Statistic, VDS, BackendStatisticResource<Host, VDS>> {

    private static final String STATISTIC_ID = UUID.nameUUIDFromBytes(STATISTICS[1].getBytes()).toString();

    private AbstractStatisticalQuery<Host, VDS> query;

    public BackendStatisticResourceTest() {
        super(new BackendStatisticResource<>(STATISTIC_ID, VDS.class, GUIDS[1], null));
    }

    @Override
    protected void init() {
        query = getQuery();
        resource.setQuery(query);
    }

    @SuppressWarnings("unchecked")
    private AbstractStatisticalQuery<Host, VDS> getQuery() {
        return mock(AbstractStatisticalQuery.class);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations(STATISTICS, true);
        Statistic statistic = resource.get();
        verify(statistic, STATISTICS[1]);
        verifyLinks(statistic);
    }

    @Test
    public void testGetBadGuid() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations(new String[] {"cpu.burnout", "cpu.meltdown", "cpu.vapourized"}, false);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    private void setUpQueryExpectations(String[] names, boolean link) throws Exception {
        VDS entity = getEntity(0);
        when(query.resolve(eq(GUIDS[1]))).thenReturn(entity);
        List<Statistic> statistics = new ArrayList<>();
        for (String name : names) {
            statistics.add(getPrototype(name));
        }
        when(query.getStatistics(same(entity))).thenReturn(statistics);
        if (link) {
            when(query.getParentType()).thenReturn(Host.class);
        }
    }

    @Override
    protected VDS getEntity(int index) {
        return mock(VDS.class);
    }
}
