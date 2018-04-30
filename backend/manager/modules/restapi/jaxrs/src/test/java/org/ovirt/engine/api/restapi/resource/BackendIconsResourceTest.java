package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIconsResourceTest
        extends AbstractBackendCollectionResourceTest<Icon, VmIcon, BackendIconsResource> {

    private static final String[] DATA_URLS = new String[] {
            "data:image/png;base64,iVB0",
            "data:image/png;base64,iVB1",
            "data:image/png;base64,iVB2",
            "data:image/png;base64,iVB3",
    };

    public BackendIconsResourceTest() {
        super(new BackendIconsResource(), null, "");
    }

    @Override
    protected List<Icon> getCollection() {
        return collection.list().getIcons();
    }

    @Override
    protected VmIcon getEntity(int index) {
        final VmIcon entity = new VmIcon();
        return setupEntityExpectations(entity, index);
    }

    private static VmIcon setupEntityExpectations(VmIcon entity, int index) {
        entity.setId(GUIDS[index]);
        entity.setDataUrl(DATA_URLS[index]);
        return entity;
    }

    @Override
    protected void verifyModel(Icon model, int index) {
        verifyIconModel(model, index);
        verifyLinks(model);
    }

    public static void verifyIconModel(Icon model, int index) {
        assertEquals(VmIcon.dataUrlToTypeAndData( DATA_URLS[index]).getFirst(), model.getMediaType());
        assertEquals(VmIcon.dataUrlToTypeAndData(DATA_URLS[index]).getSecond(), model.getData());
        assertEquals(GUIDS[index].toString(), model.getId());
    }

    @Override
     protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetAllVmIcons,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpVmIcons(),
                failure);
    }

    public static List<VmIcon> setUpVmIcons() {
        final List<VmIcon> result = new ArrayList<>();
        // iterating over NAMES, because it is shorter than GUIDS and it is used in verifyCollection() method
        for (int i = 0; i < NAMES.length; i++) {
            final VmIcon vmIcon = new VmIcon(GUIDS[i], DATA_URLS[i]);
            result.add(vmIcon);
        }
        return result;
    }
}
