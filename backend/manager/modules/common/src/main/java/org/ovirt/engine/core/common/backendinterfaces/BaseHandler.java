package org.ovirt.engine.core.common.backendinterfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;
import org.ovirt.engine.core.common.utils.Pair;

public class BaseHandler implements IObjectDescriptorContainer {
    /**
     * Returns a boolean indication regarding whether it is allowed to update a specified field of a specified object in
     * a specified status. Example for overriding this function:
     *
     * public override bool CanUpdateField(object obj, string fieldName, Enum status) { VDS vds = obj as VDS; VDSStatus
     * vdsStatus = (VDSStatus)status; switch (vdsStatus) { case Maintenance: ... ... ... default: return
     * base.CanUpdateField(....) } }
     *
     * @param obj
     *            The object to update field in.
     * @param fieldName
     *            The field to update.
     * @param status
     *            The status to consider.
     * @return True if fieldName is allowed for update, false otherwise.
     */
    @Override
    public boolean CanUpdateField(Object obj, String fieldName, Enum<?> status) {
        return true;
    }

    /**
     * scan classes for a given annotated fields. Those fields must comply to a bean property form to be used by the
     * identityChecker.
     * @param <A>
     *            the annotation class to scan for
     * @param clz
     *            class array of the scanned types.
     * @return array of pairs of a the annotation instance -> field name
     */
    public static <A extends Annotation> List<Pair<A , String>> extractAnnotatedFields(Class<A> annotation, Class<?>... clz) {
        List<Pair<A, String>> pairList = new ArrayList<Pair<A, String>>();
        for (Class<?> clazz : clz) {
            for (Field field : clazz.getDeclaredFields()) {
                A fieldAnnotation = field.getAnnotation(annotation);
                if (fieldAnnotation != null) {
                    pairList.add(new Pair<A, String>(fieldAnnotation, field.getName()));
                }
            }
        }
        return pairList;
    }
}
