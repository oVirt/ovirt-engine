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

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateCdromResourceTest
        extends AbstractBackendSubResourceTest<Cdrom, VmTemplate, BackendTemplateCdromResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid CDROM_ID = GUIDS[0];
    private static final String ISO_PATH = "Fedora-13-x86_64-Live.iso";

    public BackendTemplateCdromResourceTest() {
        super(new BackendTemplateCdromResource(CDROM_ID.toString(), TEMPLATE_ID));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(getTemplate());
        control.replay();

        Cdrom cdrom = resource.get();
        verifyModel(cdrom);
        verifyLinks(cdrom);
    }

    private VmTemplate getTemplate() {
        VmTemplate template = new VmTemplate();
        template.setId(TEMPLATE_ID);
        template.setIsoPath(ISO_PATH);
        return template;
    }

    private void setUpEntityQueryExpectations(VmTemplate result) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmTemplate,
            GetVmTemplateParameters.class,
            new String[] { "Id" },
            new Object[] { TEMPLATE_ID },
            result
        );
    }

    private void verifyModel(Cdrom model) {
        assertEquals(Guid.Empty.toString(), model.getId());
        assertTrue(model.isSetTemplate());
        assertEquals(TEMPLATE_ID.toString(), model.getTemplate().getId());
        assertTrue(model.isSetFile());
        assertEquals(ISO_PATH, model.getFile().getId());
    }
}
