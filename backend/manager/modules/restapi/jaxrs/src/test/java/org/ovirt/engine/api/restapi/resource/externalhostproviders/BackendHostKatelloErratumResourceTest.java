package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostKatelloErratumResourceTest extends AbstractBackendSubResourceTest<KatelloErratum, Erratum, BackendHostKatelloErratumResource> {
    public BackendHostKatelloErratumResourceTest() {
        super(new BackendHostKatelloErratumResource(string2hex(NAMES[1]), GUIDS[0].toString()));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
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
        Erratum erratum = mock(Erratum.class);
        when(erratum.getId()).thenReturn(GUIDS[index].toString());
        when(erratum.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return erratum;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
                QueryType.GetErratumByIdForHost,
                HostErratumQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound ? null : getEntity(0));
    }
}
