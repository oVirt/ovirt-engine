package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.jsonrpc.client.ResponseDecomposer;
import org.ovirt.vdsm.jsonrpc.client.utils.LockWrapper;

/**
 * Provides asynchronous behavior to synchronous engine code. Request is sent during construction of this map but it
 * blocks waiting for response only when it is needed so you can pass around this map and have little or no waiting on
 * response.
 *
 */
@SuppressWarnings("serial")
public class FutureMap implements Map<String, Object> {
    private final static String STATUS = "status";
    private final static String DEFAULT_KEY = "info";
    private final static long DEFAULT_RESPONSE_WAIT = 1;
    private static Log log = LogFactory.getLog(FutureMap.class);
    private final Lock lock = new ReentrantLock();
    private Future<JsonRpcResponse> response;
    private Map<String, Object> responseMap = new HashMap<String, Object>();
    private String responseKey;
    private String subtypeKey;
    private Map<String, Object> statusDone = new HashMap<String, Object>() {
        {
            super.put("message", "Done");
            super.put("code", 0);
        }
    };
    private Map<String, Object> timeoutStatus = new HashMap<String, Object>() {
        {
            super.put("message", "Internal timeout occured");
            super.put("code", -1);
        }
    };
    private Class<?> clazz = new HashMap<String, Object>().getClass();
    private Class<?> subTypeClazz;
    private boolean ignoreResponseKey = false;
    private long timeout = 0;
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    /**
     * During creation request is sent and <code>Future</code> for a response is held.
     *
     * @param client - Client object used to send request.
     * @param request - Request to be sent.
     * @throws XmlRpcRunTimeException when there are connection issues.
     */
    public FutureMap(JsonRpcClient client, JsonRpcRequest request) {
        try {
            this.response = client.call(request);
        } catch (ClientConnectionException e) {
            throw new XmlRpcRunTimeException("Connection issues during send request", e);
        }
    }

    /**
     * During creation request is sent and <code>Future</code> for a response is held.
     *
     * @param client - Client object used to send request.
     * @param request - Request to be sent.
     * @param timeout - Timeout which is used when populating response map.
     * @param unit - Time unit for timeout.
     * @throws XmlRpcRunTimeException when there are connection issues.
     */
    public FutureMap(JsonRpcClient client, JsonRpcRequest request, long timeout, TimeUnit unit) {
        try {
            this.timeout = timeout;
            this.unit = unit;
            this.response = client.call(request);
        } catch (ClientConnectionException e) {
            throw new XmlRpcRunTimeException("Connection issues during send request", e);
        }
    }

    /**
     * Whenever any method is executed to obtain value of response during the first invocation it gets real response
     * from the <code>Future</code> and decompose it to object of provided type and structure.
     *
     * This method blocks waiting on response or error.
     */
    private void lazyEval() {
        try (LockWrapper wrapper = new LockWrapper(this.lock)) {
            if (this.responseMap.isEmpty()) {
                try {
                    if (timeout != 0) {
                        populate(this.response.get(timeout, unit));
                    } else {
                        populate(this.response.get());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Exception occured during response decomposition", e);
                    throw new IllegalStateException(e);
                } catch (TimeoutException e) {
                    this.responseMap.put(STATUS, timeoutStatus);
                }
            }
        }
    }

    private void populate(JsonRpcResponse response) {
        ResponseDecomposer decomposer = new ResponseDecomposer(response);
        if (decomposer.isError()) {
            this.responseMap = decomposer.decomposeError();
        } else if (Object[].class.equals(clazz) && this.subtypeKey != null && !this.subtypeKey.trim().isEmpty()
                && this.subTypeClazz != null) {
            Object[] array = (Object[]) decomposer.decomposeResponse(this.clazz);
            updateResponse(decomposer.decomposeTypedArray(array, this.subTypeClazz, subtypeKey));
        } else {
            updateResponse(decomposer.decomposeResponse(this.clazz));
        }
        checkAndUpdateStatus();
    }

    /**
     * Whenever any method is executed to obtain value of response during the first invocation it gets real response
     * from the <code>Future</code> and decompose it to object of provided type and structure.
     *
     * This method waits for a response or error for specified amount of time.
     *
     * @param wait - time in seconds how long we want to wait for response.
     */
    private void lazyEval(long wait) {
        try (LockWrapper wrapper = new LockWrapper(this.lock)) {
            if (this.responseMap.isEmpty()) {
                try {
                    populate(this.response.get(wait, TimeUnit.SECONDS));
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.debug("Response not arrived yet");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateResponse(Object object) {
        if (ignoreResponseKey) {
            this.responseMap = (Map<String, Object>) object;
        } else {
            String key = DEFAULT_KEY;
            if (this.responseKey != null && !this.responseKey.trim().isEmpty()) {
                key = responseKey;
            }
            this.responseMap.put(key, object);
        }
    }

    private void checkAndUpdateStatus() {
        if (this.responseMap.get(STATUS) == null) {
            this.responseMap.put(STATUS, statusDone);
        }
    }

    @Override
    public void clear() {
        lazyEval();
        this.responseMap.clear();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        lazyEval();
        return this.responseMap.entrySet();
    }

    @Override
    public boolean isEmpty() {
        lazyEval();
        return this.responseMap.isEmpty();
    }

    public boolean isDone() {
        lazyEval(DEFAULT_RESPONSE_WAIT);
        return !this.responseMap.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        lazyEval();
        return this.responseMap.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        lazyEval();
        return this.responseMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        lazyEval();
        this.responseMap.putAll(map);
    }

    @Override
    public int size() {
        lazyEval();
        return this.responseMap.size();
    }

    @Override
    public Collection<Object> values() {
        lazyEval();
        return this.responseMap.values();
    }

    @Override
    public boolean containsKey(Object key) {
        lazyEval();
        return this.responseMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        lazyEval();
        return this.responseMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        lazyEval();
        return this.responseMap.get(key);
    }

    @Override
    public Object remove(Object key) {
        lazyEval();
        return this.responseMap.remove(key);
    }

    /**
     * @param responseKey
     *            - Key used to store response value in result <code>Map</code>.
     * @return this <code>FutureMap</code>.
     */
    public FutureMap withResponseKey(String responseKey) {
        this.responseKey = responseKey;
        return this;
    }

    /**
     * @param clazz- A type of response which will be use instead of default <code>Map</code>.
     * @return this <code>FutureMap</code>.
     */
    public FutureMap withResponseType(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * During response decomposition we will ignore default key and use raw response structure as result
     * <code>Map</code>.
     *
     * @return this <code>FutureMap</code>.
     */
    public FutureMap withIgnoreResponseKey() {
        this.ignoreResponseKey = true;
        return this;
    }

    /**
     * @param subTypeKey - Key which is used to put subtype to result map.
     * @return this <code>FutureMap</code>.
     */
    public FutureMap withSubtypeKey(String subTypeKey) {
        this.subtypeKey = subTypeKey;
        return this;
    }

    /**
     * @param clazz - type of the subtype.
     * @return this <code>FutureMap</code>.
     */
    public FutureMap withSubTypeClazz(Class<?> clazz) {
        this.subTypeClazz = clazz;
        return this;
    }
}
