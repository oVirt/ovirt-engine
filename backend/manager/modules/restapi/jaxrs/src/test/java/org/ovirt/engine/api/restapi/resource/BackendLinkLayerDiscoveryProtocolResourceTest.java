package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.LinkLayerDiscoveryProtocolElement;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendLinkLayerDiscoveryProtocolResourceTest
        extends AbstractBackendCollectionResourceTest<LinkLayerDiscoveryProtocolElement, Tlv,
        BackendLinkLayerDiscoveryProtocolResource>{

    private static final Guid NIC_ID = GUIDS[0];
    private static final String PROPERTY_NAME = "propertyName";


    public BackendLinkLayerDiscoveryProtocolResourceTest() {
        super(new BackendLinkLayerDiscoveryProtocolResource(NIC_ID), null, null);
    }

    @Test
    public void testQueryFail() {

        setUpEmptyQueryExpectations();
        assertEquals(0, getCollection().size());
    }

    @Override
    protected List<LinkLayerDiscoveryProtocolElement> getCollection() {
        return collection.list().getLinkLayerDiscoveryProtocolElements();
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpQueryExpectations(query, failure, false);

    }

    private void setUpEmptyQueryExpectations() {
        setUpQueryExpectations(null, null, true);
    }

    protected void setUpQueryExpectations(String query, Object failure, boolean empty) {
        setUpEntityQueryExpectations(QueryType.GetTlvsByHostNicId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{NIC_ID},
                empty ? null : getEntityList(),
                failure);
    }

    public static List<Nic> setUpInterfaces() {
        List<Nic> nics = new ArrayList<>();
        nics.add(getNic());
        return nics;
    }

    public static Nic getNic() {
        Nic nic = new Nic();
        nic.setId(NIC_ID);
        return nic;
    }

    protected List<Tlv> getEntityList() {
        List<Tlv> entities = new ArrayList<>();
        for(int index=0; index< NAMES.length; index++) {
            Tlv tlv = new Tlv();
            tlv.setName(NAMES[index]);
            tlv.setType(getType(index));
            tlv.setOui(getOui(index));
            tlv.setSubtype(getSubType(index));
            tlv.getProperties().put(PROPERTY_NAME, getPropertyValue(index));
            entities.add(tlv);
        }

        return entities;
    }

    @Override
    protected void verifyModel(LinkLayerDiscoveryProtocolElement model, int index) {
        assertEquals(NAMES[index], model.getName());
        assertEquals(getType(index), model.getType().intValue());
        assertEquals(getOui(index), model.getOui().intValue());
        assertEquals(getSubType(index), model.getSubtype().intValue());
        List<Property> properties = model.getProperties().getProperties();
        assertEquals(1, properties.size());
        Property property = properties.get(0);
        assertEquals(PROPERTY_NAME, properties.get(0).getName());
        assertEquals(getPropertyValue(index), properties.get(0).getValue());
    }

    private int getType(int index) {
        return index;
    }

    private int getOui(int index) {
        return 2*index;
    }

    private int getSubType(int index) {
        return 3*index;
    }

    private String getPropertyValue(int index) {
        return Integer.toString(4*index);
    }
}
