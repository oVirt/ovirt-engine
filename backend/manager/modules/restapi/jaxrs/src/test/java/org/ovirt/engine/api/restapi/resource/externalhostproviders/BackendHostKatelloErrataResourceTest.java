/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostKatelloErrataResourceTest extends AbstractBackendCollectionResourceTest<KatelloErratum, Erratum, BackendHostKatelloErrataResource> {
    public BackendHostKatelloErrataResourceTest() {
        super(new BackendHostKatelloErrataResource(GUIDS[0].toString()), null, "");
    }

    @Override
    protected List<KatelloErratum> getCollection() {
        return collection.list().getKatelloErrata();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
                QueryType.GetErrataForHost,
                GetErrataCountsParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getErrataData(),
                failure);
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
        Erratum erratum = mock(Erratum.class);
        when(erratum.getId()).thenReturn(NAMES[index]);
        when(erratum.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return erratum;
    }

    @Override
    protected void verifyModel(KatelloErratum model, int index) {
        assertEquals(NAMES[index], hex2string(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
