package io.onemfive.core.sensors.i2p.bote.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Same as <code>java.util.Properties</code> but writes properties
 * sorted by key. Relies on an implementation detail, so it may
 * not work on JVMs other than OpenJDK, or future versions.
 */
public class SortedProperties extends Properties {
    private static final long serialVersionUID = -3663917284130106235L;

    @Override
    public synchronized Enumeration<Object> keys() {
        Enumeration<Object> unsorted = super.keys();
        List<Object> list = Collections.list(unsorted);
        Collections.sort(list, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1==null && o2==null)
                    return 0;
                else if (o1 == null)
                    return -1;
                else if (o2 == null)
                    return 1;
                else
                    return o1.toString().compareTo(o2.toString());
            }
        });
        Enumeration<Object> sorted = Collections.enumeration(list);
        return sorted;
    }
}
