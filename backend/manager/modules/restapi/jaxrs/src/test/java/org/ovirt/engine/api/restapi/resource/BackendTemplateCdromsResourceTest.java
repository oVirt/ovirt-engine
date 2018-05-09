/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendTemplateCdromsResourceTest
    extends AbstractBackendCollectionResourceTest<Cdrom, VmTemplate, BackendTemplateCdromsResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final String ISO_PATH = "Fedora-13-x86_64-Live.iso";

    public BackendTemplateCdromsResourceTest() {
        super(new BackendTemplateCdromsResource(TEMPLATE_ID), null, null);
    }

    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }

    private void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    private void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { TEMPLATE_ID },
                getTemplate(),
                failure
            );
        }
    }

    private VmTemplate getTemplate() {
        VmTemplate template = new VmTemplate();
        template.setId(TEMPLATE_ID);
        template.setIsoPath(ISO_PATH);
        return template;
    }

    protected List<Cdrom> getCollection() {
        return collection.list().getCdroms();
    }

    @Test
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getCdromResource("foo")));
    }

    @Override
    protected void verifyCollection(List<Cdrom> collection) {
        assertNotNull(collection);
        assertEquals(1, collection.size());
        verifyModel(collection.get(0));
    }

    private void verifyModel(Cdrom model) {
        assertEquals(Guid.Empty.toString(), model.getId());
        assertTrue(model.isSetTemplate());
        assertEquals(TEMPLATE_ID.toString(), model.getTemplate().getId());
        assertTrue(model.isSetFile());
        assertEquals(ISO_PATH, model.getFile().getId());
        verifyLinks(model);
    }
}
