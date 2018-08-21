package io.onemfive.core.infovault.graph;

import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.Set;

public class GraphUtil {

    public static void updateProperties(Node node, Map<String,Object> attributes) {
        Set<String> keys = attributes.keySet();
        for(String key : keys) {
            node.setProperty(key,attributes.get(key));
        }
    }

}
