package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendSystemKatelloErrataResourceTest extends AbstractBackendCollectionResourceTest<KatelloErratum, Erratum, BackendSystemKatelloErrataResource> {
    public BackendSystemKatelloErrataResourceTest() {
        super(new BackendSystemKatelloErrataResource(), null, "");
    }

    @Override
    protected List<KatelloErratum> getCollection() {
        return collection.list().getKatelloErrata();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
                VdcQueryType.GetErrataForSystem,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getErrata(),
                failure);
        control.replay();
    }

    private List<Erratum> getErrata() {
        List<Erratum> errata = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            errata.add(getEntity(i));
        }

        return errata;
    }

    @Override
    protected Erratum getEntity(int index) {
        Erratum erratum = control.createMock(Erratum.class);
        expect(erratum.getId()).andReturn(NAMES[index]).anyTimes();
        expect(erratum.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return erratum;
    }

    @Override
    protected void verifyModel(KatelloErratum model, int index) {
        assertEquals(NAMES[index], hex2string(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
