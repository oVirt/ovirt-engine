package org.ovirt.engine.core.bll.gluster.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

public class GlusterEventsProcessorTest {

    @Test
    public void testEventsSerialize() {
        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        String jsonBody="{'event': 'BRICK_CONNECTED', 'message': "
                + "{'peer': 'SERVER1', "
                + "'volume': 'vmstore', 'brick': '/gluster_bricks/vmstore/vmstore'}, "
                + "'nodeid': '82e53643-48ca-4808-a759-dfaaebc28914', 'ts': 1554962105}";
        jsonBody = jsonBody.replace("\'", "\"");
        GlusterEvent event = deserializer.deserializeUnformattedJson(jsonBody, GlusterEvent.class);
        assertEquals("BRICK_CONNECTED", event.getEvent());
    }

    @Test
    public void testEvents() {
        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        String jsonBody = " { 'nodeid': '95cd599c-5d87-43c1-8fba-b12821fd41b6', \n" +
                "   'ts': 1468303352, \n" +
                "   'event': 'EVENT_GEOREP_CHECKPOINT_COMPLETED', \n" +
                "   'message': \n" +
                "   { 'master_volume': 'MASTER_VOL', \n" +
                "     'slave_host': 'SLAVE_HOST',\n" +
                "     'slave_volume': 'SLAVE_VOLUME', \n" +
                "     'brick_path': 'BRICK_PATH', \n" +
                "     'checkpoint_time': 1468303352, \n" +
                "     'checkpoint_completion_time': 1468303352 } }";
        jsonBody = jsonBody.replace("\'", "\"");
        GlusterEvent event = deserializer.deserializeUnformattedJson(jsonBody, GlusterEvent.class);
        assertEquals("EVENT_GEOREP_CHECKPOINT_COMPLETED", event.getEvent());
    }

}
