package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticKind;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Value;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Values;
import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.core.common.businessentities.VDS;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStatisticsResourceTest extends AbstractBackendCollectionResourceTest<Statistic, VDS, BackendStatisticsResource<Host, VDS>> {

    static final String[] STATISTICS = { "data.corrupted", "data.burned", "data.mislaid" };

    private AbstractStatisticalQuery<Host, VDS> query;

    public BackendStatisticsResourceTest() {
        super(new BackendStatisticsResource<>(VDS.class, GUIDS[1], null), null, "");
    }

    @Override
    protected void init() {
        query = getQuery();
        collection.setQuery(query);
        initResource(collection);
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testSubResourceLocator() {
        String id = UUID.nameUUIDFromBytes(STATISTICS[1].getBytes()).toString();
        assertTrue(collection.getStatisticResource(id) instanceof StatisticResource);
    }

    @SuppressWarnings("unchecked")
    private AbstractStatisticalQuery<Host, VDS> getQuery() {
        return (AbstractStatisticalQuery<Host, VDS>) mock(AbstractStatisticalQuery.class);
    }

    static Statistic getPrototype(String name) {
        Statistic statistic = new Statistic();
        statistic.setName(name);
        statistic.setId(UUID.nameUUIDFromBytes(statistic.getName().getBytes()).toString());
        statistic.setUnit(StatisticUnit.BYTES);
        statistic.setKind(StatisticKind.GAUGE);
        Value value = new Value();
        statistic.setValues(new Values());
        statistic.setType(ValueType.INTEGER);
        statistic.getValues().getValues().add(value);
        statistic.setHost(new Host());
        statistic.getHost().setId(GUIDS[2].toString());
        return statistic;
    }

    @Override
    protected void setUpQueryExpectations(String unused) throws Exception {
        setUpQueryExpectations(unused, null);
    }

    protected void setUpQueryExpectations(String unused, final Object failure) throws Exception {
        VDS entity = getEntity(0);
        if (failure == null) {
            when(query.resolve(eq(GUIDS[1]))).thenReturn(entity);
            List<Statistic> statistics = new ArrayList<>();
            for (String name : STATISTICS) {
                statistics.add(getPrototype(name));
            }
            when(query.getStatistics(same(entity))).thenReturn(statistics);
            when(query.getParentType()).thenReturn(Host.class);
        } else  if (failure instanceof String) {
            String detail = mockl10n((String)failure);
            when(query.resolve(eq(GUIDS[1]))).thenThrow(new BaseBackendResource.BackendFailureException(detail, Status.CONFLICT));
        } else  if (failure instanceof Exception) {
            when(query.resolve(eq(GUIDS[1]))).thenThrow((Exception) failure);
        }
    }

    @Override
    protected VDS getEntity(int index) {
        return mock(VDS.class);
    }

    @Override
    protected List<Statistic> getCollection() {
        return collection.list().getStatistics();
    }

    @Override
    protected String getSubResourceId() {
        return getPrototype(STATISTICS[1]).getId();
    }

    @Override
    protected void verifyCollection(List<Statistic> collection) {
        assertNotNull(collection);
        assertEquals(STATISTICS.length, collection.size());
        for (int i = 0; i < STATISTICS.length; i++) {
            Statistic statistic = collection.get(i);
            verify(statistic, STATISTICS[i]);
            verifyLinks(statistic);
        }
    }

    static void verify(Statistic statistic, String name) {
        assertEquals(UUID.nameUUIDFromBytes(name.getBytes()).toString(), statistic.getId());
        assertEquals(name, statistic.getName());
        assertEquals(StatisticUnit.BYTES, statistic.getUnit());
        assertEquals(StatisticKind.GAUGE, statistic.getKind());
        assertTrue(statistic.isSetValues());
        assertEquals(ValueType.INTEGER, statistic.getType());
    }
}
