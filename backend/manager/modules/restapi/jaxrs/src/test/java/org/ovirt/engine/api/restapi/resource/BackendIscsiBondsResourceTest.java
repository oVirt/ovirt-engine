package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddIscsiBondParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIscsiBondsResourceTest
    extends AbstractBackendCollectionResourceTest<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond, BackendIscsiBondsResource> {

    protected static final Guid ISCSI_BOND_ID = GUIDS[1];
    protected static final Guid DATA_CENTER_ID = GUIDS[2];

    public BackendIscsiBondsResourceTest() {
        super(new BackendIscsiBondsResource(DATA_CENTER_ID.toString()), null, "");
    }

    @Override
    protected List<IscsiBond> getCollection() {
        return collection.list().getIscsiBonds();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.IscsiBond getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
                new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(GUIDS[index]);
        iscsiBond.setStoragePoolId(DATA_CENTER_ID);
        iscsiBond.setName(NAMES[1]);
        iscsiBond.setDescription(DESCRIPTIONS[1]);
        return iscsiBond;
    }

    @Test
    public void testAddIscsiBond() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddIscsiBond,
                AddIscsiBondParameters.class,
                new String[] { "IscsiBond" },
                new Object[] { getIscsiBond() },
                true,
                true,
                getIscsiBond().getId(),
                QueryType.GetIscsiBondById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { ISCSI_BOND_ID },
                getEntity(1));

        Response response = collection.add(getIscsiBondApi());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof IscsiBond);
        verifyModel((IscsiBond) response.getEntity(), 1);
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBond() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
                new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        iscsiBond.setStoragePoolId(DATA_CENTER_ID);
        iscsiBond.setName(NAMES[0]);
        return iscsiBond;
    }

    private IscsiBond getIscsiBondApi() {
        IscsiBond iscsiBond = new IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID.toString());
        iscsiBond.setName(NAMES[0]);
        return iscsiBond;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetIscsiBondsByStoragePoolId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DATA_CENTER_ID },
                setUpIscsiBonds(),
                failure);
    }

    static List<org.ovirt.engine.core.common.businessentities.IscsiBond> setUpIscsiBonds() {
        List<org.ovirt.engine.core.common.businessentities.IscsiBond> iscsiBonds = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
                    new org.ovirt.engine.core.common.businessentities.IscsiBond();
            iscsiBond.setDescription(DESCRIPTIONS[i]);
            iscsiBond.setName(NAMES[i]);
            iscsiBond.setId(GUIDS[i]);
            iscsiBond.setStoragePoolId(DATA_CENTER_ID);
            iscsiBonds.add(iscsiBond);
        }
        return iscsiBonds;
    }


}
