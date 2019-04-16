package org.ovirt.engine.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.gluster.events.GlusterEventsProcessor;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterEventsWebHookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(GlusterEventsWebHookServlet.class);

    @Inject
    private GlusterEventsProcessor glusterEventsProcessor;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: verify signature
        try {
            String jsonBody = readBody(request.getReader());

            // Deserialiaze content if not empty
            if (!jsonBody.isEmpty()) {
                JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
                GlusterEvent event = deserializer.deserializeUnformattedJson(jsonBody, GlusterEvent.class);
                processEvent(event);
            }
            response.setStatus(HttpURLConnection.HTTP_OK);

        } catch (IOException ex) {
            log.error("Error reading event data", ex.getMessage());
            log.debug("Exception", ex);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (Exception e) {
            log.error("Error processing event data", e.getMessage());
            log.debug("Exception", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private String readBody(BufferedReader body) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int r;
        while ((r = body.read()) != -1) {
            buffer.append((char) r);
        }
        return buffer.toString();
    }

    private void processEvent(GlusterEvent event) {
        glusterEventsProcessor.processEvent(event);
    }

}
