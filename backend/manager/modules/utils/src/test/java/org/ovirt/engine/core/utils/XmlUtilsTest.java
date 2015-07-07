package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class XmlUtilsTest {
    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    private int level;
    private String value;

    public XmlUtilsTest(int level, String value) {
        this.level = level;
        this.value = value;
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> params = new ArrayList<>();
        // XmlUtils.getXYZValue support recursive searching from a depth of 2 and above, e.g.:
        // <tag><innerTag>value</innerTag><tag>
        for (int i = 2; i < 10; ++i) {
            params.add(new Object[]{i, String.valueOf(RandomUtils.instance().nextInt())});
        }
        return params;
    }

    @Test
    public void testGetIntValueNesting() throws Exception {
        int actual = XmlUtils.getIntValue(stringToElement(buildNestedXml(level, value)), "Level1");
        assertEquals(Integer.valueOf(value).intValue(), actual);
    }

    @Test
    public void testGetTextValue() throws Exception {
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
