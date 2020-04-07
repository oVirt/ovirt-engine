package org.ovirt.engine.core.bll.storage.disk.image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ImageioClient {

    public static final String TICKETS_URI = "/tickets/";
    public static final int CLIENT_BUFFER_SIZE = 4 * 1024;

    private String hostname;
    private int port;

    public ImageioClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public ImageTicketInformation getTicket(Guid ticketUUID) {
        // Create request
        BasicHttpRequest request = new BasicHttpRequest(
                HttpMethod.GET, TICKETS_URI + ticketUUID, HttpVersion.HTTP_1_1);
        String responseContent;

        try (DefaultBHttpClientConnection conn = getConnection()) {
            HttpEntity response = executeRequest(request, conn);
            responseContent = readContent(response.getContent());
            EntityUtils.consume(response);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return ImageTicketInformationHelper.fromJson(responseContent);
    }

    public void putTicket(ImageTicket ticket) {
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
                HttpMethod.PUT, TICKETS_URI + ticket.getId(), HttpVersion.HTTP_1_1);

        try (DefaultBHttpClientConnection conn = getConnection()) {
            // Populate ticket in the request
            StringEntity entity = new StringEntity(
                    new ObjectMapper().writeValueAsString(ticket.toDict()), StandardCharsets.UTF_8);
            request.setEntity(entity);
            request.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
            EntityUtils.consume(executeRequest(request, conn));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void extendTicket(Guid ticketUUID, long timeout) {
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
                HttpMethod.PATCH, TICKETS_URI + ticketUUID, HttpVersion.HTTP_1_1);

        try (DefaultBHttpClientConnection conn = getConnection()) {
            // Populate timeout in the request
            Map<String, Object> timeoutDict = new HashMap<>();
            timeoutDict.put("timeout", timeout);
            StringEntity entity = new StringEntity(
                    new ObjectMapper().writeValueAsString(timeoutDict), StandardCharsets.UTF_8);
            request.setEntity(entity);
            request.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
            EntityUtils.consume(executeRequest(request, conn));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteTicket(Guid ticketUUID) {
        BasicHttpRequest request = new BasicHttpRequest(
                HttpMethod.DELETE, TICKETS_URI + ticketUUID, HttpVersion.HTTP_1_1);

        try (DefaultBHttpClientConnection conn = getConnection()) {
            EntityUtils.consume(executeRequest(request, conn));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private DefaultBHttpClientConnection getConnection() {
        DefaultBHttpClientConnection conn;
        try {
            Socket socket = new Socket(hostname, port);
            // Bind socket to HTTP client
            conn = new DefaultBHttpClientConnection(CLIENT_BUFFER_SIZE);
            conn.bind(socket);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return conn;
    }

    protected HttpEntity executeRequest(BasicHttpRequest request, DefaultBHttpClientConnection conn) {
        try {
            // Send the request
            conn.sendRequestHeader(request);
            if (request instanceof HttpEntityEnclosingRequest) {
                conn.sendRequestEntity((HttpEntityEnclosingRequest) request);
            }
            conn.flush();

            // Get the response
            HttpResponse response = conn.receiveResponseHeader();
            conn.receiveResponseEntity(response);

            // Check status
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException(String.format(
                        "ImageioClient request failed. Status: %s, Reason: %s, Error: %s.",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase(),
                        readContent(response.getEntity().getContent())));
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new RuntimeException("Empty response");
            }

            // Return the HTTP entity
            return entity;
        } catch (HttpException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String readContent(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining());
    }
}
