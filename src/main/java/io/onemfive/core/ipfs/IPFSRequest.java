package io.onemfive.core.ipfs;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class IPFSRequest {
    public enum PinType {all, direct, indirect, recursive}
    public String requestedOperation;
    public NamedStreamable file;
    public List<NamedStreamable> files;
    public Multipart multipart;
    public Multihash hash;
    public String subPath;
    public Boolean recursive;
    public String domain;
    public String scheme;
    public File ipfsRoot;
    public File ipnsRoot;
    public PinType pinType;
    public List<byte[]> dataBytesList;
    public String template;
    public Multihash base;
    public String command;
    public byte[] dataBytes;
    public String name;
    public Multihash target;
    public String id;
    public MultiAddress addr;
    public String key;
    public String value;
    public Boolean all;
    public String multiAddr;
    public String targetStr;
    public String path;
    public URL targetURL;
    public byte[] bodyBytes;
}
