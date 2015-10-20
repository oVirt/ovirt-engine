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

import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateCdromsResourceTest
    extends AbstractBackendCollectionResourceTest<Cdrom, VmTemplate, BackendTemplateCdromsResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final String ISO_PATH = "Fedora-13-x86_64-Live.iso";

    public BackendTemplateCdromsResourceTest() {
        super(new BackendTemplateCdromsResource(TEMPLATE_ID), null, null);
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    private void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    private void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmTemplate,
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
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getCdromResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    protected void verifyCollection(List<Cdrom> collection) throws Exception {
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
