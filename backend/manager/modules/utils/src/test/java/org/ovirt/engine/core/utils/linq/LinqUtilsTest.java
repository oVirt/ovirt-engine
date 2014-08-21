package org.ovirt.engine.core.utils.linq;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 9, 2009 Time: 4:05:52 PM To change this template use File |
 * Settings | File Templates.
 */
public class LinqUtilsTest {
    private List<String> list = new LinkedList<String>() {
        {
            add("one");
            add("two");
            add("three");
        }
    };
    private List<String> cutlist = new LinkedList<String>() {
        {
            add("o");
            add("t");
            add("t");
        }
    };

    @Test
    public void exampleFirstOrNull() {
        String string = LinqUtils.firstOrNull(list, new Predicate<String>() {
            @Override
            public boolean eval(String s) {
                return s.equals("two");
            }
        });

        assertEquals("two", string);
    }

    @Test
    public void exampleForeach() {
        List<String> results = LinqUtils.transformToList(list, new Function<String, String>() {
            @Override
            public String eval(String s) {
                return s.substring(0, 1);
            }
        });

        assertEquals(results, cutlist);
    }

    @Test
    public void exampleFilter() {
        List<String> filteredList = LinqUtils.filter(list, new Predicate<String>() {
            @Override
            public boolean eval(String s) {
                return "two".equals(s);
            }
        });

        assertEquals("two", filteredList.get(0));
    }

    @Test
    public void exampleToMap() {
        Map<String, String> map = LinqUtils.toMap(list, new Mapper<String, String, String>() {
            @Override
            public String createKey(String s) {
                return s.substring(0, 1);
            }

            @Override
            public String createValue(String s) {
                return s;
            }
        });

        Map<String, String> example = new HashMap<String, String>() {
            {
                put("t", "three");
                put("o", "one");
            }
        };

        assertEquals(map, example);
    }

    @Test
    public void exampleToMapDefaultMapper() {
        Map<String, String> map = LinqUtils.toMap(list, new DefaultMapper<String, String>() {
            @Override
            public String createKey(String s) {
                return s.substring(0, 1);
            }
        });

        Map<String, String> example = new HashMap<String, String>() {
            {
                put("t", "three");
                put("o", "one");
            }
        };

        assertEquals(map, example);
    }

    @Test
    public void exampleAggregate() {
        String r = LinqUtils.aggregate(Arrays.asList("a", "b", "c"), new Aggregator<String>() {
            @Override
            public String process(String s, String s1) {
                return s + ":" + s1;
            }
        });

        assertEquals("a:b:c", r);
    }
}
