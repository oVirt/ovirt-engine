/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class BackendTemplateCdromResourceTest
        extends AbstractBackendSubResourceTest<Cdrom, VmTemplate, BackendTemplateCdromResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid CDROM_ID = GUIDS[0];
    private static final String ISO_PATH = "Fedora-13-x86_64-Live.iso";

    public BackendTemplateCdromResourceTest() {
        super(new BackendTemplateCdromResource(CDROM_ID.toString(), TEMPLATE_ID));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(getTemplate());

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

    private void setUpEntityQueryExpectations(VmTemplate result) {
        setUpEntityQueryExpectations(
            QueryType.GetVmTemplate,
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
