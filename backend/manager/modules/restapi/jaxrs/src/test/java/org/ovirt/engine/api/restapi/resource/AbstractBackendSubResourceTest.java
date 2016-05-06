package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Statistic;

public abstract class AbstractBackendSubResourceTest<R extends BaseResource, Q /* extends IVdcQueryable */, S extends AbstractBackendSubResource<R, Q>>
        extends AbstractBackendResourceTest<R, Q> {

    protected static final String IMMUTABLE_REASON_SERVER_LOCALE = "Scheiterte Unveranderlichkeit Einschrankung";
    protected static final String IMMUTABLE_ID_DETAIL_SERVER_LOCALE = "Versuchte Anderung der unveranderlichen Eigenschaft: id";
    protected static int CONFLICT = 409;
    protected static long Kb = 1024L;
    protected static long Mb = 1024*Kb;

    protected S resource;

    protected AbstractBackendSubResourceTest(S resource) {
        this.resource = resource;
    }

    protected void init() {
        initResource(resource);
    }

    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubCollectionInjection() throws Exception {
        control.replay();
        // walk super-interface hierarchy to find non-inherited method annotations
        injectSubCollectionAndTest();
    }

    protected void injectSubCollectionAndTest() throws IllegalAccessException, InvocationTargetException {
        for (Class<?> resourceInterface : resource.getClass().getInterfaces()) {
            for (Method method : resourceInterface.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Path.class) && isSubCollectionLocator(method)) {
                    Object rawSubResource = method.invoke(resource);
                    if (rawSubResource instanceof AbstractBackendResource) {
                        AbstractBackendResource<R, Q> subResource = (AbstractBackendResource<R, Q>)rawSubResource;
                        assertNotNull(subResource.getBackend());
                        assertNotNull(subResource.getMappingLocator());
                    }
                }
            }
        }
    }

    protected boolean isSubCollectionLocator(Method method) {
        return method.getName().startsWith("get")
               && method.getName().toLowerCase().endsWith("resource")
               && method.getParameterTypes().length == 0
               && method.getReturnType() != null;
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async) throws Exception {
        verifyActionResponse(r, baseUri, async, null);
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async, String reason) throws Exception {
        assertEquals("unexpected status", async ? 202 : 200, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect Action response entity", entity instanceof Action);
        Action action = (Action)entity;
        if (async) {
            assertTrue(action.isAsync());
            assertNotNull(action.getHref());
            assertNotNull(action.getId());
            assertNotNull(action.getLinks());
            assertEquals(2, action.getLinks().size());
            assertEquals("expected parent link", "parent", action.getLinks().get(0).getRel());
            assertNotNull(action.getLinks().get(0).getHref());
            assertTrue(action.getLinks().get(0).getHref().startsWith(BASE_PATH + "/" + baseUri));
            assertNotNull(action.getLinks().get(1).getHref());
            assertEquals("expected replay link", "replay", action.getLinks().get(1).getRel());
            assertTrue(action.getLinks().get(1).getHref().startsWith(BASE_PATH + "/" + baseUri));
        } else {
            assertTrue(!(action.isSetAsync() && action.isAsync()));
        }

        assertTrue("unexpected status", async
                   ? action.getStatus().equals(CreationStatus.PENDING.value())
                     || action.getStatus().equals(CreationStatus.IN_PROGRESS.value())
                     || action.getStatus().equals(CreationStatus.COMPLETE.value())
                   : reason == null
                     ? action.getStatus().equals(CreationStatus.COMPLETE.value())
                     : action.getStatus().equals(CreationStatus.FAILED.value()));
    }

    protected void verifyImmutabilityConstraint(WebApplicationException wae) {
        verifyFault(wae, IMMUTABLE_REASON_SERVER_LOCALE, IMMUTABLE_ID_DETAIL_SERVER_LOCALE, CONFLICT);
    }

    protected void verifyStatistics(List<Statistic> statistics, String[] names, BigDecimal[] values) {
        assertNotNull(statistics);
        assertEquals(names.length, statistics.size());
        for (Statistic statistic : statistics) {
            assertTrue(statistic.isSetValues());
            boolean found = false;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(statistic.getName())) {
                    assertEquals("unexpected value for: " + names[i], values[i], getDatum(statistic));
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("unexpected statistic: " + statistic.getName());
            }
        }
    }

    protected BigDecimal getDatum(Statistic statistic) {
        return statistic.getValues().getValues().get(0).getDatum();
    }

    protected BigDecimal asDec(long l) {
        return new BigDecimal(l);
    }

    protected BigDecimal asDec(double d) {
        return new BigDecimal(d, new MathContext(2));
    }
}
