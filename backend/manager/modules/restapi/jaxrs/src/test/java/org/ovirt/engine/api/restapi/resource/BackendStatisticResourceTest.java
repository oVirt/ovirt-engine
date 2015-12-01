package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.STATISTICS;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.getPrototype;
import static org.ovirt.engine.api.restapi.resource.BackendStatisticsResourceTest.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.VDS;

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
        return (AbstractStatisticalQuery<Host, VDS>)control.createMock(AbstractStatisticalQuery.class);
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
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    private void setUpQueryExpectations(String[] names, boolean link) throws Exception {
        VDS entity = getEntity(0);
        expect(query.resolve(eq(GUIDS[1]))).andReturn(entity);
        List<Statistic> statistics = new ArrayList<>();
        for (String name : names) {
            statistics.add(getPrototype(name));
        }
        expect(query.getStatistics(same(entity))).andReturn(statistics);
        if (link) {
            expect(query.getParentType()).andReturn(Host.class);
        }
        control.replay();
    }

    @Override
    protected VDS getEntity(int index) {
        VDS entity = control.createMock(VDS.class);
        return entity;
    }
}
