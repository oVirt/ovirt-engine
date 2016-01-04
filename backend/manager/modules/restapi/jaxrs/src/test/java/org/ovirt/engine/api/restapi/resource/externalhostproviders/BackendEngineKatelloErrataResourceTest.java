package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendEngineKatelloErrataResourceTest extends AbstractBackendCollectionResourceTest<KatelloErratum, Erratum, BackendEngineKatelloErrataResource> {
    public BackendEngineKatelloErrataResourceTest() {
        super(new BackendEngineKatelloErrataResource(), null, "");
    }

    @Override
    protected List<KatelloErratum> getCollection() {
        return collection.list().getKatelloErrata();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
                VdcQueryType.GetErrataForEngine,
                GetErrataCountsParameters.class,
                new String[] {},
                new Object[] {},
                getErrataData(),
                failure);
        control.replay();
    }

    private ErrataData getErrataData() {
        ErrataData errataData = new ErrataData();
        errataData.setErrata(getErrata());
        return errataData;
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
