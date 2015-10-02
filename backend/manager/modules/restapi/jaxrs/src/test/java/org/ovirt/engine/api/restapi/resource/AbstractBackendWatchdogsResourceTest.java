package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

@Ignore
public abstract class AbstractBackendWatchdogsResourceTest<R extends AbstractBackendReadOnlyDevicesResource<Watchdog, Watchdogs, VmWatchdog>>
        extends AbstractBackendCollectionResourceTest<Watchdog, VmWatchdog, R> {
    public AbstractBackendWatchdogsResourceTest(R collection) {
        super(collection, null, "");
    }

    protected static final Guid PARENT_ID = GUIDS[1];

    @Override
    protected List<Watchdog> getCollection() {
        return collection.list().getWatchdogs();
    }

    @Override
    protected VmWatchdog getEntity(int index) {
        final VmWatchdog wd = new VmWatchdog();
        wd.setId(GUIDS[index]);
        wd.setVmId(PARENT_ID);
        wd.setAction(VmWatchdogAction.RESET);
        wd.setModel(VmWatchdogType.i6300esb);
        return wd;
    }

    @Override
    /**
     * This method needed to be overridden, because the superclass is creating 3 returned VmWatchdog
     * There can only be 1 or 0.
     */
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(prefix + query, searchType);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            List<VmWatchdog> entities = new ArrayList<VmWatchdog>();
            entities.add(getEntity(0));
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        expect(backend.runQuery(eq(VdcQueryType.Search), eqSearchParams(params))).andReturn(
                queryResult).anyTimes();
        expect(backend.runQuery(eq(VdcQueryType.GetWatchdog), EasyMock.anyObject(IdQueryParameters.class))).andReturn(
                queryResult).anyTimes();

        control.replay();
    }

    @Override
    /**
     * This method needed to be overridden because the super implementation assumes that there can be multiple
     * watchdogs, while there can be only 0 or 1.
     */
    protected void verifyCollection(List<Watchdog> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(1, collection.size());
        if(!collection.isEmpty()) {
            verifyModel(collection.get(0), 0);
        }
    }

    @Override
    /**
     * This method needed to be overridden to disable name and description vaildation as watchdogs have none of these.
     */
    protected void verifyModel(Watchdog model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        verifyLinks(model);
    }

}
