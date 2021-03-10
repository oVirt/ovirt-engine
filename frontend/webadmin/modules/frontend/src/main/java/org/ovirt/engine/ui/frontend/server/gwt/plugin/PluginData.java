package org.ovirt.engine.ui.frontend.server.gwt.plugin;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Immutable structure that contains UI plugin descriptor/configuration data and associated logic.
 */
public class PluginData implements Comparable<PluginData> {

    public interface ValidationCallback {

        void descriptorError(String message);

        void configurationError(String message);

    }

    private static final String ATT_NAME = "name"; //$NON-NLS-1$
    private static final String ATT_URL = "url"; //$NON-NLS-1$
    private static final String ATT_CONFIG = "config"; //$NON-NLS-1$
    private static final String ATT_RESOURCEPATH = "resourcePath"; //$NON-NLS-1$
    private static final String ATT_LAZYLOAD = "lazyLoad"; //$NON-NLS-1$
    private static final String ATT_ENABLED = "enabled"; //$NON-NLS-1$
    private static final String ATT_ORDER = "order"; //$NON-NLS-1$

    private final JsonNode descriptorNode;
    private final long descriptorLastModified;

    private final JsonNode configurationNode;
    private final long configurationLastModified;

    private final JsonNodeFactory nodeFactory;

    public PluginData(JsonNode descriptorNode, long descriptorLastModified,
            JsonNode configurationNode, long configurationLastModified,
            JsonNodeFactory nodeFactory) {
        this.descriptorNode = descriptorNode;
        this.descriptorLastModified = descriptorLastModified;
        this.configurationNode = configurationNode;
        this.configurationLastModified = configurationLastModified;
        this.nodeFactory = nodeFactory;
    }

    public JsonNode getDescriptorNode() {
        return descriptorNode.deepCopy();
    }

    public JsonNode getConfigurationNode() {
        return configurationNode.deepCopy();
    }

    public long getDescriptorLastModified() {
        return descriptorLastModified;
    }

    public long getConfigurationLastModified() {
        return configurationLastModified;
    }

    public boolean validate(ValidationCallback callback) {
        boolean isValid = true;

        // Validate descriptor data
        if (!checkRequiredNonEmptyStringNode(descriptorNode, ATT_NAME)) {
            callback.descriptorError("Required non-empty string attribute: " + ATT_NAME); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkRequiredNonEmptyStringNode(descriptorNode, ATT_URL)) {
            callback.descriptorError("Required non-empty string attribute: " + ATT_URL); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkOptionalObjectNode(descriptorNode, ATT_CONFIG)) {
            callback.descriptorError("Optional attribute must be object: " + ATT_CONFIG); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkOptionalNonEmptyStringNode(descriptorNode, ATT_RESOURCEPATH)) {
            callback.descriptorError("Optional attribute must be non-empty string: " + ATT_RESOURCEPATH); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkOptionalBooleanNode(descriptorNode, ATT_LAZYLOAD)) {
            callback.descriptorError("Optional attribute must be boolean: " + ATT_LAZYLOAD); //$NON-NLS-1$
            isValid = false;
        }

        // Validate configuration data
        if (!checkOptionalObjectNode(configurationNode, ATT_CONFIG)) {
            callback.configurationError("Optional attribute must be object: " + ATT_CONFIG); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkOptionalBooleanNode(configurationNode, ATT_ENABLED)) {
            callback.configurationError("Optional attribute must be boolean: " + ATT_ENABLED); //$NON-NLS-1$
            isValid = false;
        }
        if (!checkOptionalIntegerNode(configurationNode, ATT_ORDER)) {
            callback.configurationError("Optional attribute must be integer: " + ATT_ORDER); //$NON-NLS-1$
            isValid = false;
        }

        return isValid;
    }

    boolean checkRequiredNonEmptyStringNode(JsonNode root, String fieldName) {
        JsonNode target = root.path(fieldName);
        return !target.isMissingNode() && target.isTextual() && !target.textValue().trim().isEmpty();
    }

    boolean checkOptionalNonEmptyStringNode(JsonNode root, String fieldName) {
        JsonNode target = root.path(fieldName);
        return !target.isMissingNode() ? target.isTextual() && !target.textValue().trim().isEmpty() : true;
    }

    boolean checkOptionalObjectNode(JsonNode root, String fieldName) {
        JsonNode target = root.path(fieldName);
        return !target.isMissingNode() ? target.isObject() : true;
    }

    boolean checkOptionalBooleanNode(JsonNode root, String fieldName) {
        JsonNode target = root.path(fieldName);
        return !target.isMissingNode() ? target.isBoolean() : true;
    }

    boolean checkOptionalIntegerNode(JsonNode root, String fieldName) {
        JsonNode target = root.path(fieldName);
        return !target.isMissingNode() ? target.isIntegralNumber() : true;
    }

    /**
     * Applies custom (user-defined) configuration on top of default (descriptor-defined) configuration, and returns the
     * resulting JSON object.
     */
    public ObjectNode mergeConfiguration() {
        JsonNode descriptorConfigNode = descriptorNode.path(ATT_CONFIG);
        ObjectNode result = nodeFactory.objectNode();

        // Apply default configuration, if any
        if (!descriptorConfigNode.isMissingNode() && descriptorConfigNode.isObject()) {
            result.putAll((ObjectNode) descriptorConfigNode);
        }

        // Apply custom configuration, if any
        JsonNode customConfigNode = configurationNode.path(ATT_CONFIG);
        if (customConfigNode != null && !customConfigNode.isMissingNode() && customConfigNode.isObject()) {
            result.putAll((ObjectNode) customConfigNode);
        }

        return result;
    }

    public String getName() {
        return descriptorNode.path(ATT_NAME).textValue();
    }

    public String getUrl() {
        return descriptorNode.path(ATT_URL).textValue();
    }

    public String getResourcePath() {
        JsonNode target = descriptorNode.path(ATT_RESOURCEPATH);
        return !target.isMissingNode() ? target.textValue() : null;
    }

    public boolean isLazyLoad() {
        JsonNode target = descriptorNode.path(ATT_LAZYLOAD);
        return !target.isMissingNode() ? target.booleanValue() : true;
    }

    public boolean isEnabled() {
        JsonNode target = configurationNode.path(ATT_ENABLED);
        return !target.isMissingNode() ? target.booleanValue() : true;
    }

    public int getOrder() {
        JsonNode target = configurationNode.path(ATT_ORDER);
        return !target.isMissingNode() ? target.intValue() : Integer.MAX_VALUE;
    }

    /**
     * Natural comparison method based on {@linkplain #getOrder plugin load order}.
     */
    @Override
    public int compareTo(PluginData other) {
        if (this == other) {
            return 0;
        }

        int o1 = this.getOrder();
        int o2 = other.getOrder();

        if (o1 < o2) {
            return -1;
        } else if (o1 > o2) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PluginData)) {
            return false;
        }

        PluginData other = (PluginData) obj;
        return Objects.equals(getOrder(), other.getOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getOrder());
    }

    @Override
    public String toString() {
        return "Plugin " + getName(); //$NON-NLS-1$
    }

}
