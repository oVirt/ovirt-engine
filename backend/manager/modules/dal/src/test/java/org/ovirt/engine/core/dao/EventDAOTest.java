package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;

public class EventDAOTest extends BaseDAOTestCase {
    private static final int FREE_AUDIT_LOG_ID = 44295;
    private static final int EVENT_MAP_COUNT = 1;
    private EventDAO dao;
    private Guid existingSubscriber;
    private Guid newSubscriber;
    private event_subscriber newSubscription;
    private event_notification_hist newHistory;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getEventDao();
        existingSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
        newSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
        newSubscription = new event_subscriber();
        newSubscription.setsubscriber_id(newSubscriber);
        newSubscription.setmethod_id(1);
        newSubscription.setevent_up_name("TestRun");
        newSubscription.settag_name("farkle");

        newHistory = new event_notification_hist();
        newHistory.setaudit_log_id(FREE_AUDIT_LOG_ID);
        newHistory.setevent_name("Failure");
        newHistory.setmethod_type("Email");
        newHistory.setreason("Dunno");
        newHistory.setsent_at(new Date());
        newHistory.setstatus(false);
        newHistory.setsubscriber_id(existingSubscriber);
    }

    /**
     * Ensures that retrieving all subscribers works as expected.
     */
    @Test
    public void testGetAll() {
        List<event_subscriber> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty collection is returned when the user has no subscriptions.
     */
    @Test
    public void testGetAllForSubscriberWithNoSubscriptions() {
        List<event_subscriber> result = dao.getAllForSubscriber(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all subscriptions are returned.
     */
    @Test
    public void testGetAllForSubscriber() {
        List<event_subscriber> result = dao
                .getAllForSubscriber(existingSubscriber);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (event_subscriber subscription : result) {
            assertEquals(existingSubscriber,
                    subscription.getsubscriber_id());
        }
    }

    @Test
    public void testGetEventNotificationMethodsById() {
        List<EventNotificationMethod> result = dao.getEventNotificationMethodsById(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    /**
     * Ensures that subscribing a user works as expected.
     */
    @Test
    public void testSubscribe() {
        dao.subscribe(newSubscription);

        List<event_subscriber> result = dao.getAllForSubscriber(newSubscription
                .getsubscriber_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (event_subscriber subscription : result) {
            assertEquals(newSubscriber, subscription.getsubscriber_id());
        }
    }

    /**
     * Ensures that updating a subscription works as expected.
     */
    @Test
    public void testUpdate() {
        event_subscriber before = dao
                .getAllForSubscriber(existingSubscriber).get(0);

        int oldMethodId = before.getmethod_id();
        before.setmethod_id(2);

        dao.update(before, oldMethodId);

        event_subscriber after = dao
                .getAllForSubscriber(existingSubscriber).get(0);

        assertNotNull(after);
        assertEquals(before, after);
    }

    /**
     * Ensures that unsubscribing a user works as expected.
     */
    @Test
    public void testUnsubscribe() {
        List<event_subscriber> before = dao
                .getAllForSubscriber(existingSubscriber);

        // ensure we have subscriptions
        assertFalse(before.isEmpty());
        for (event_subscriber subscriber : before) {
            dao.unsubscribe(subscriber);
        }

        List<event_subscriber> after = dao
                .getAllForSubscriber(existingSubscriber);

        assertNotNull(after);
        assertTrue(after.isEmpty());
    }

    @Test
    public void testGetEventMapByNameWithInvalidName() {
        List<EventMap> result = dao.getEventMapByName("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEventMapByName() {
        List<EventMap> result = dao.getEventMapByName("TestRun");

        assertNotNull(result);
        for (EventMap mapping : result) {
            assertEquals("TestRun", mapping.getEventUpName());
        }
    }

    @Test
    public void testGetAllEventMaps() {
        List<EventMap> result = dao.getAllEventMaps();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(EVENT_MAP_COUNT, result.size());
    }

}
