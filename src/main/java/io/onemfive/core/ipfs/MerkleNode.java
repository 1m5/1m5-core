package io.onemfive.core.ipfs;

import io.onemfive.data.util.JSONParser;

import java.util.*;

public class MerkleNode {
    public final Multihash hash;
    public final String name;
    public final Integer size;
    public final Integer type;
    public final List<MerkleNode> links;
    public final byte[] data;

    public MerkleNode(String hash) {
        this(hash,null,null,null,new ArrayList<MerkleNode>(),null);
    }

    public MerkleNode(String hash, String name) {
        this(hash, name, null, null, new ArrayList<MerkleNode>(), null);
    }

    public MerkleNode(String hash, String name, Integer size, Integer type, List<MerkleNode> links, byte[] data) {
        this.name = name;
        this.hash = Multihash.fromBase58(hash);
        this.size = size;
        this.type = type;
        this.links = links;
        this.data = data;
    }

    @Override
    public boolean equals(Object b) {
        if (!(b instanceof MerkleNode))
            return false;
        MerkleNode other = (MerkleNode) b;
        return hash.equals(other.hash); // ignore name hash says it all
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    public static MerkleNode fromJSON(Object rawjson) {
        if (rawjson instanceof String)
            return new MerkleNode((String)rawjson);
        Map json = (Map)rawjson;
        String hash = (String)json.get("Hash");
        if (hash == null)
            hash = (String)json.get("Key");
        String name = json.containsKey("Name") ? (String) json.get("Name"): null;
        Integer size = json.containsKey("Size") ? (Integer) json.get("Size"): null;
        Integer type = json.containsKey("Type") ? (Integer) json.get("Type"): null;
        List<Object> linksRaw = (List<Object>) json.get("Links");
        List<MerkleNode> links = new ArrayList<>();
        if(linksRaw != null) {
            for(Object x : linksRaw) {
                links.add(fromJSON(x));
            }
        }
        byte[] data = json.containsKey("Data") ? ((String)json.get("Data")).getBytes(): null;
        return new MerkleNode(hash, name, size, type, links, data);
    }

    public Object toJSON() {
        Map res = new TreeMap<>();
        res.put("Hash", hash);
        List<Object> linksRaw = new ArrayList<>();
        for(MerkleNode x : links) {
            linksRaw.add(x.toJSON());
        }
        res.put("Links", linksRaw);
        if (data != null)
            res.put("Data", data);
        if (name != null)
            res.put("Name", name);
        if (size != null)
            res.put("Size", size);
        if (type != null)
            res.put("Type", type);
        return res;
    }

    public String toJSONString() {
        return JSONParser.toString(toJSON());
    }
}
