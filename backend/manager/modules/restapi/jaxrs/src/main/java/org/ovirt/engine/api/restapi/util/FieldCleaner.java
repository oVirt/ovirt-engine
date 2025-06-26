package org.ovirt.engine.api.restapi.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Link;

public class FieldCleaner {

    private FieldCleaner() { }

    public static void removeAllLinksExcept(BaseResource baseResource, String... allowedRel) {
        List<Link> links = baseResource.getLinks();
        if (CollectionUtils.isEmpty(links)) {
            return;
        }

        Set<String> allowedRelSet = Set.of(allowedRel);
        links.removeIf(link -> !allowedRelSet.contains(link.getRel()));
    }

    public static void nullifyAllFieldsExcept(Object obj, String... allowedFields) {
        if (obj == null) {
            return;
        }

        Set<String> allowedFieldSet = Set.of(allowedFields);
        Class<?> currentClass = obj.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!allowedFieldSet.contains(field.getName())) {
                    nullifyField(obj, field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private static void nullifyField(Object obj, Field field) {
        try {
            field.setAccessible(true);
            if (!field.getType().isPrimitive()) {
                field.set(obj, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to nullify field: " + field.getName(), e);
        }
    }
}
