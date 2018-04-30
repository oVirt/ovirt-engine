package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@ExtendWith(RandomUtilsSeedingExtension.class)
public class XmlUtilsTest {
    public static Stream<Arguments> data() {
        // XmlUtils.getXYZValue support recursive searching from a depth of 2 and above, e.g.:
        // <tag><innerTag>value</innerTag><tag>
        return IntStream.range(2, 10).mapToObj(i -> Arguments.of(i, String.valueOf(RandomUtils.instance().nextInt())));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testGetIntValueNesting(int level, String value) throws Exception {
        int actual = XmlUtils.getIntValue(stringToElement(buildNestedXml(level, value)), "Level1");
        assertEquals(Integer.valueOf(value).intValue(), actual);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testGetTextValue(int level, String value) throws Exception {
        String actual = XmlUtils.getTextValue(stringToElement(buildNestedXml(level, value)), "Level1");
        assertEquals(value, actual);
    }

    private static Element stringToElement(String s) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(s.getBytes("UTF-8")))
                .getDocumentElement();
    }

    private static String buildNestedXml (int count, String s) {
        if (count == 0) {
            return s;
        }
        return "<Level" + count + '>' + buildNestedXml(count - 1, s) + "</Level" + count + '>';
    }
}
