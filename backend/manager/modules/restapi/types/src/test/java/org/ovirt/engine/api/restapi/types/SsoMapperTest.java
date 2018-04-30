package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Method;
import org.ovirt.engine.api.model.Methods;
import org.ovirt.engine.api.model.Sso;
import org.ovirt.engine.core.common.businessentities.SsoMethod;

public class SsoMapperTest {

    @Test
    public void mapGuestAgentSsoFromBackendToRest() {
        SsoMethod backendSsoMethod = SsoMethod.GUEST_AGENT;

        Sso restSso = SsoMapper.map(backendSsoMethod, null);

        assertNotNull(restSso);
        assertNotNull(restSso.getMethods());
        assertNotNull(restSso.getMethods().getMethods());
        assertEquals(1, restSso.getMethods().getMethods().size());
        assertEquals(org.ovirt.engine.api.model.SsoMethod.GUEST_AGENT, restSso.getMethods().getMethods().get(0).getId());
    }

    @Test
    public void mapNoneSsoFromBackendToRest() {
        SsoMethod backendSsoMethod = SsoMethod.NONE;

        Sso restSso = SsoMapper.map(backendSsoMethod, null);

        assertNotNull(restSso);
        assertNotNull(restSso.getMethods());
        assertNotNull(restSso.getMethods().getMethods());
        assertTrue(restSso.getMethods().getMethods().isEmpty());
    }

    @Test
    public void mapGuestAgentSsoFromRestToBackend() {
        Sso restSso = new Sso();
        restSso.setMethods(new Methods());
        Method guestAgent = new Method();
        guestAgent.setId(org.ovirt.engine.api.model.SsoMethod.GUEST_AGENT);
        restSso.getMethods().getMethods().add(guestAgent);

        SsoMethod expectedBackendSsoMethod = SsoMethod.GUEST_AGENT;

        assertEquals(expectedBackendSsoMethod, SsoMapper.map(restSso, null));
    }

    @Test
    public void mapNoMethodSsoFromRestToBackend() {
        Sso restSso = new Sso();
        restSso.setMethods(new Methods());

        SsoMethod expectedBackendSsoMethod = SsoMethod.NONE;

        assertEquals(expectedBackendSsoMethod, SsoMapper.map(restSso, null));
    }

    @Test
    public void mapIncompleteSsoFromRestToBackend() {
        Sso restSso = new Sso();

        assertNull(SsoMapper.map(restSso, null));
    }
}
