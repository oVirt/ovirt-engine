package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostKatelloErratumResourceTest extends AbstractBackendSubResourceTest<KatelloErratum, Erratum, BackendHostKatelloErratumResource> {
    public BackendHostKatelloErratumResourceTest() {
        super(new BackendHostKatelloErratumResource(string2hex(NAMES[1]), GUIDS[0].toString()));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        control.replay();
        verifyModel(resource.get(), 0);
    }

    @Override
    protected void verifyModel(KatelloErratum model, int index) {
        assertEquals(GUIDS[index].toString(), hex2string(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }

    @Override
    protected Erratum getEntity(int index) {
        Erratum erratum = control.createMock(Erratum.class);
        expect(erratum.getId()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(erratum.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return erratum;
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(
                VdcQueryType.GetErratumByIdForHost,
                HostErratumQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound ? null : getEntity(0));
    }
}
