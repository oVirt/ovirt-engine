/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

public class JAXBHelper {
    private static final Map<Class<?>, JAXBContextHolder> contexts = new HashMap<>();

    private JAXBHelper() {}

    /**
     * It's unfortunate that's there's no clone already defined on JAXB
     * generated classes. Here we emulate that missing deep copy support
     * by marshalling and unmarshalling (a little heavyweight admittedly).
     *
     * @param <S>    type parameter
     * @param object object to be cloned
     * @param clz    type of that object
     * @return       clone
     */
    public static <S> S clone(JAXBElement<S> element) {
        S ret = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshall(baos, element);
            ret = unmarshall(new ByteArrayInputStream(baos.toString().getBytes()), element.getDeclaredType());
        } catch (Exception e) {
        }
        return ret;
    }

    public static <S> S clone(String localName, Class<S> clz, S s) {
        return clone(new JAXBElement<>(new QName("", localName), clz, null, s));
    }

    private static <S> void marshall(OutputStream os, JAXBElement<S> element) throws Exception {
        Marshaller marshaller = getContext(element.getDeclaredType()).getMarshaller();
        synchronized (marshaller) {
            marshaller.marshal(element, os);
        }
    }

    private static <S> S unmarshall(InputStream is, Class<S> clz) throws Exception {
        Unmarshaller unmarshaller = getContext(clz).getUnmarshaller();
        synchronized (unmarshaller) {
            JAXBElement<S> root = unmarshaller.unmarshal(new StreamSource(is), clz);
            return root.getValue();
        }
    }

    private static synchronized JAXBContextHolder getContext(Class<?> clz) throws JAXBException {
        if (!contexts.containsKey(clz)) {
            contexts.put(clz, new JAXBContextHolder(JAXBContext.newInstance(clz)));
        }
        return contexts.get(clz);
    }

    private static class JAXBContextHolder {
        Unmarshaller unmarshaller;
        Marshaller marshaller;

        public JAXBContextHolder(JAXBContext context) throws JAXBException {
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();
        }

        public Unmarshaller getUnmarshaller() {
            return unmarshaller;
        }

        public Marshaller getMarshaller() {
            return marshaller;
        }
    }
}
