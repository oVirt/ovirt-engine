package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.Date;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;

public class ErratumMapperTest extends AbstractInvertibleMappingTest<KatelloErratum, Erratum, Erratum> {

    private static final Date DATE = new Date();

    public ErratumMapperTest() {
        super(KatelloErratum.class, Erratum.class, Erratum.class);
    }

    @Override
    protected void verify(KatelloErratum model, KatelloErratum transform) {
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getTitle(), transform.getTitle());
        assertEquals(model.getSummary(), transform.getSummary());
        assertEquals(model.getSolution(), transform.getSolution());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getIssued(), transform.getIssued());
        assertEquals(model.getSeverity(), transform.getSeverity());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(model.getPackages());
        assertNotNull(transform.getPackages());
        assertEquals(model.getPackages().getPackages().size(), transform.getPackages().getPackages().size());
        for (int i = 0; i < model.getPackages().getPackages().size(); i++) {
            assertEquals(model.getPackages().getPackages().get(i).getName(),
                    transform.getPackages().getPackages().get(i).getName());
        }
    }

    protected KatelloErratum postPopulate(KatelloErratum model) {
        model.setName(model.getId());
        model.setId(string2hex(model.getId()));
        model.setIssued(DateMapper.map(DATE, null));
        model.setSeverity(ErrataSeverity.MODERATE.getDescription());
        model.setType(ErrataType.ENHANCEMENT.getDescription());
        return super.postPopulate(model);
    }
}
