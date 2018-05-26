package io.onemfive.core.ipfs;

import io.onemfive.core.BaseService;
import io.onemfive.core.Config;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.*;
import io.onemfive.data.util.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Abstracts IPFS as an internal Service.
 *
 * https://discuss.ipfs.io/t/writeable-http-gateways/210
 *
 * @author objectorange
 */
public class IPFSService extends BaseService {

    // Gateways
    public static final String OPERATION_GATEWAY_LIST = "GATEWAY_LIST";
    public static final String OPERATION_GATEWAY_ADD = "GATEWAY_ADD";
//    public static final String OPERATION_GATEWAY_RM = "GATEWAY_RM";
    public static final String OPERATION_GATEWAY_GET = "GATEWAY_GET";

    // Local
    public static final String OPERATION_ADD = "ADD";
    public static final String OPERATION_LIST = "LIST";
    public static final String OPERATION_CAT = "CAT";
    public static final String OPERATION_GET = "GET";
    public static final String OPERATION_REFS = "REFS";
    public static final String OPERATION_REFS_LOCAL = "REFS_LOCAL";
    public static final String OPERATION_RESOLVE = "RESOLVE";
    public static final String OPERATION_DNS = "DNS";
    public static final String OPERATION_MOUNT = "MOUNT";

    public static final String OPERATION_ADD_PIN = "ADD_PIN";
    public static final String OPERATION_LIST_PINS = "LIST_PINS";
    public static final String OPERATION_LIST_PINS_BY_TYPE = "LIST_PINS_BY_TYPE";
    public static final String OPERATION_RM_PIN = "RM_PIN";
    public static final String OPERATION_RM_PIN_RECURSIVE = "RM_PIN_RECURSIVE";

    public static final String OPERATION_BLOCK_GET = "BLOCK_GET";
    public static final String OPERATION_BLOCK_PUT = "BLOCK_PUT";
    public static final String OPERATION_BLOCK_STAT = "BLOCK_STAT";

    public static final String OPERATION_OBJECT_PUT = "OBJECT_PUT";
    public static final String OPERATION_OBJECT_PUT_ENCODED = "OBJECT_PUT_ENCODED";
    public static final String OPERATION_OBJECT_GET = "OBJECT_GET";
    public static final String OPERATION_OBJECT_LINKS = "OBJECT_LINKS";
    public static final String OPERATION_OBJECT_STAT = "OBJECT_STAT";
    public static final String OPERATION_OBJECT_DATA = "OBJECT_DATA";
    public static final String OPERATION_OBJECT_NEW = "OBJECT_NEW";
    public static final String OPERATION_OBJECT_PATCH = "OBJECT_PATCH";

    public static final String OPERATION_NAME_PUBLISH = "NAME_PUBLISH";
    public static final String OPERATION_NAME_RESOLVE = "NAME_RESOLVE";

    public static final String OPERATION_DHT_FINDPROVS = "DHT_FINDPROVS";
    public static final String OPERATION_DHT_QUERY = "DHT_QUERY";
    public static final String OPERATION_DHT_FINDPEER = "DHT_FINDPEER_OPERATION";
    public static final String OPERATION_DHT_GET = "DHT_GET";
    public static final String OPERATION_DHT_PUT= "DHT_PUT";

    public static final String OPERATION_FILE_LIST = "FILE_LIST";

    // Network
    public static final String OPERATION_BOOTSTRAP_LIST_PEERS = "BOOTSTRAP_LIST_PEERS";
    public static final String OPERATION_ADD_PEER = "BOOTSTRAP_ADD_PEER";
    public static final String OPERATION_RM_PEER = "BOOTSTRAP_RM_PEER";

    public static final String OPERATION_SWARM_LIST_PEERS = "SWARM_LIST_PEERS";
    public static final String OPERATION_SWARM_ADDRS = "SWARM_ADDRS";
    public static final String OPERATION_SWARM_CONNECT = "SWARM_CONNECT";
    public static final String OPERATION_SWARM_DISCONNECT = "SWARM_DISCONNECT";

    public static final String OPERATION_DIAG_NET = "DIAG_NET";
    public static final String OPERATION_PING = "PING";
    public static final String OPERATION_ID = "ID";
    public static final String OPERATION_STATS_BW = "STATS_BW";

    // Tools
    public static final String OPERATION_VERSION = "VERSION";
    public static final String OPERATION_COMMANDS = "COMMANDS";
    public static final String OPERATION_LOG_TAIL = "LOG_TAIL";
    public static final String OPERATION_CONFIG_SHOW = "CONFIG_SHOW";
    public static final String OPERATION_CONFIG_REPLACE = "CONFIG_REPLACE";
    public static final String OPERATION_CONFIG_GET = "CONFIG_GET";
    public static final String OPERATION_CONFIG_SET = "CONFIG_SET";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_UPDATE_CHECK = "UPDATE_CHECK";
    public static final String OPERATION_UPDATE_LOG = "UPDATE_LOG";

    // Build results from network
    public static final String OPERATION_PACK = "PACK";

    // Properties
    private static final String PROP_IPFS_LOCAL = "1m5.ipfs.local";
    private static final String PROP_IPFS_ENCODING = "1m5.ipfs.encoding";
    private static final String PROP_IPFS_VERSION = "1m5.ipfs.version";

    private static final String PROP_GATEWAYS_LIST = "1m5.ipfs.gateways.checker";

    private static final String PROP_USE_TOR = "1m5.ipfs.gateways.useTor";
    private static final String PROP_TOR_GATEWAYS = "1m5.ipfs.gateways.tor";
    private static final String PROP_CLEARNET_GATEWAYS = "1m5.ipfs.gateways.clearnet";

    public static final String MIN_VERSION = "0.4.3";
    public List<String> ObjectTemplates = Arrays.asList("unixfs-dir");
    public List<String> ObjectPatchTypes = Arrays.asList("add-link", "rm-link", "set-data", "append-data");

    private Properties config;
    private String version;

    private String local;
    private String encoding = "UTF-8";

    private String gatewaysListUrl;
    private boolean useTor = false;
    private enum GatewayStatus {Active, Inactive, Unknown}
    private Map<String,GatewayStatus> torGateways = new HashMap<>();
    private Map<String,GatewayStatus> clearnetGateways = new HashMap<>();

    public IPFSService() {
        super();
    }

    public IPFSService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope e) {
        // Request for IPFS Service
        boolean isRequest = true;
        e.setHeader(Envelope.HEADER_CONTENT_TYPE,"application/json; charset=utf-8");
        IPFSRequest request = (IPFSRequest)DLC.getData(IPFSRequest.class, e);
        Route route = e.getRoute();
        String operation = route.getOperation();
        String contentStr = "";
        byte[] contentBytes = null;
        IPFSResponse response = null;
        if(operation.equals(OPERATION_PACK)) {
            System.out.println(IPFSService.class.getSimpleName()+": handling IPFSResponse...");
            operation = request.requestedOperation;
            isRequest = false;
            contentBytes = (byte[])DLC.getContent(e);
            contentStr = new String(contentBytes);
            response = new IPFSResponse();
            DLC.addData(IPFSResponse.class, response, e);
        } else {
            System.out.println(IPFSService.class.getSimpleName()+": handling IPFSRequest: "+request);
        }
        String urlStr = "";
//        Boolean snapshot = (Boolean)((DocumentMessage)e.getMessage()).data.get(0).get(DLC.SNAPSHOT);
//        if(snapshot != null && !snapshot) {
//            urlStr += gateway.replace("/ipfs/","/ipfn/");
//        }
        switch(operation){
            case OPERATION_GATEWAY_LIST: {
                if(isRequest) {
                    System.out.println(IPFSService.class.getSimpleName()+": Sending Gateway List request...");
                    urlStr = gatewaysListUrl;
                    e.setAction(Envelope.Action.VIEW);
                } else {
                    System.out.println(IPFSService.class.getSimpleName()+": Received Gateway List response: ");
                    Map<String, String> gateways = new HashMap<>();
                    List<Object> objects = (List<Object>)JSONParser.parse(contentStr);
                    String gateway;
                    GatewayStatus status;
                    for(Object obj : objects){
                        gateway = (String)obj;
                        status = clearnetGateways.get(gateway);
                        gateways.put(gateway,status.name());
                        System.out.println(gateway+":"+status.name());
                    }
                    response.gateways = gateways;
                }
                break;
            }
            case OPERATION_GATEWAY_ADD: {
                if(isRequest) {
                    urlStr = getActiveGateway().replace(":hash", "");
                    Multipart m = new Multipart(encoding);
                    if (request.files == null) {
                        request.files = new ArrayList<>();
                    }
                    if (request.file != null) {
                        request.files.add(request.file);
                    }
                    for (NamedStreamable file : request.files) {
                        try {
                            if (file.isDirectory()) {
                                m.addSubtree("", ((FileWrapper) file).getFile());
                            } else {
                                m.addFilePart(file.getName(), file);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                    request.multipart = m;
                } else {
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_GATEWAY_GET: {
                if(isRequest) {
                    urlStr = getActiveGateway().replace(":hash", request.hash.toString());
                } else {
                    response.resultBytes = contentBytes;
                }
                break;
            }
//            case OPERATION_GATEWAY_RM: {
//                if(isRequest) {
//                    urlStr = getActiveGateway().replace(":hash", request.hash.toString());
//                } else {
//
//                }
//                break;
//            }
            case OPERATION_ADD: {
                if(isRequest) {
                    urlStr = local + version + "add?stream-channels=true";
                    Multipart m = new Multipart(encoding);
                    if (request.files == null) {
                        request.files = new ArrayList<>();
                    }
                    if (request.file != null) {
                        request.files.add(request.file);
                    }
                    for (NamedStreamable file : request.files) {
                        try {
                            if (file.isDirectory()) {
                                m.addSubtree("", ((FileWrapper) file).getFile());
                            } else {
                                m.addFilePart("file", file);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                    request.multipart = m;
                } else {
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_LIST: {
                if(isRequest) {
                    urlStr = local + version + "ls/" + request.hash.toString();
                } else {
                    Map res = (Map)JSONParser.parse(contentStr);
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = (List<Object>)res.get("Objects");
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_CAT: {
                if(isRequest) {
                    if (request.subPath == null) {
                        urlStr = local + version + "cat/" + request.hash;
                    } else {
                        try {
                            urlStr = local + version + "cat?arg=" + request.hash.toString() + URLEncoder.encode(request.subPath, encoding);
                        } catch (UnsupportedEncodingException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                } else {
                    response.resultBytes = contentBytes;
                }
                break;
            }
            case OPERATION_GET: {
                if(isRequest) {
                    urlStr = local + version + "get/" + request.hash.toString();
                } else {
                    response.resultBytes = contentBytes;
                }
                break;
            }
            case OPERATION_REFS: {
                if(isRequest) {
                    urlStr = local + version
                            + "refs?arg=" + request.hash.toString()
                            + "&r=" + request.recursive.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            // level 2 command
            case OPERATION_REFS_LOCAL: {
                if(isRequest) {
                    urlStr = local + version + "refs/local";
                } else {
                    List<Multihash> multihashs = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        multihashs.add(Multihash.fromBase58(((String)((Map)JSONParser.parse(object)).get("Ref"))));
                    }
                }
                break;
            }
            case OPERATION_RESOLVE: {
                if(isRequest) {
                    urlStr = local + version
                            + "resolve?arg=/" + request.scheme
                            + "/" + request.hash.toString()
                            + "&r=" + request.recursive.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DNS: {
                if(isRequest) {
                    urlStr = local + version + "dns?arg=" + request.domain;
                } else {
                    response.resultString = (String)((Map)JSONParser.parse(contentStr)).get("Path");
                }
                break;
            }
            case OPERATION_MOUNT: {
                if(isRequest) {
                    if(request.ipfsRoot != null && !request.ipfsRoot.exists()) {
                        request.ipfsRoot.mkdirs();
                    }
                    if (request.ipnsRoot != null && !request.ipnsRoot.exists())
                        request.ipnsRoot.mkdirs();
                    urlStr = local + version
                            + "mount?arg=" +
                            (request.ipfsRoot != null ? request.ipfsRoot.getPath() : "/ipfs")
                            + "&arg=" +
                            (request.ipnsRoot != null ? request.ipnsRoot.getPath() : "/ipns");
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            // Pinning an object ensures a local copy of it is kept.
            case OPERATION_ADD_PIN: {
                if(isRequest) {
                    urlStr = local + version
                            + "pin/add?stream-channels=true&arg="
                            + request.hash.toString();
                } else {
                    List<Object> objects = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Pins");
                    List<Multihash> pins = new ArrayList<>();
                    for(Object obj : objects) {
                        pins.add(Multihash.fromBase58((String)obj));
                    }
                    response.multihashs = pins;
                }
                break;
            }
            case OPERATION_LIST_PINS_BY_TYPE: {
                if(isRequest) {
                    urlStr = local + version
                            + "pin/ls?stream-channels=true&t="
                            + request.pinType.name();
                } else {
                    Map<Multihash, Object> pins = new HashMap<>();
                    Map<String,Object> objMap = (Map<String,Object>)((Map)JSONParser.parse(contentStr)).get("Keys");
                    for(Map.Entry entry : objMap.entrySet()) {
                        pins.put(Multihash.fromBase58((String)entry.getKey()),entry.getValue());
                    }
                    response.pins = pins;
                }
                break;
            }
            case OPERATION_LIST_PINS: {
                if(isRequest) {
                    urlStr = local + version
                            + "pin/ls?stream-channels=true&t="
                            + IPFSRequest.PinType.direct;
                } else {
                    Map<Multihash, Object> pins = new HashMap<>();
                    Map<String,Object> objMap = (Map<String,Object>)((Map)JSONParser.parse(contentStr)).get("Keys");
                    for(Map.Entry entry : objMap.entrySet()) {
                        pins.put(Multihash.fromBase58((String)entry.getKey()),entry.getValue());
                    }
                    response.pins = pins;
                }
                break;
            }
            case OPERATION_RM_PIN_RECURSIVE: {
                if(isRequest) {
                    urlStr = local + version
                            + "pin/rm?stream-channels=true&r=" + request.recursive.toString()
                            + "&arg=" + request.hash.toString();
                } else {
                    List<Multihash> pins = new ArrayList<>();
                    List<Object> objects = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Pins");
                    for(Object obj : objects) {
                        pins.add(Multihash.fromBase58((String)obj));
                    }
                    response.multihashs = pins;
                }
                break;
            }
            case OPERATION_RM_PIN: {
                if(isRequest) {
                    urlStr = local + version
                            + "pin/rm?stream-channels=true&r=true"
                            + "&arg=" + request.hash.toString();
                } else {
                    List<Multihash> pins = new ArrayList<>();
                    List<Object> objects = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Pins");
                    for(Object obj : objects) {
                        pins.add(Multihash.fromBase58((String)obj));
                    }
                    response.multihashs = pins;
                }
                break;
            }
            // 'ipfs block' is a plumbing command used to manipulate raw ipfs blocks.
            case OPERATION_BLOCK_GET: {
                if(isRequest) {
                    urlStr = local + version + "block/get?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    response.resultBytes = contentBytes;
                }
                break;
            }
            case OPERATION_BLOCK_PUT: {
                if(isRequest) {
                    urlStr = local + version + "block/put?stream-channels=true";
                    Multipart m = new Multipart(encoding);
                    for (byte[] f : request.dataBytesList) {
                        try {
                            m.addFilePart("file", new ByteArrayWrapper(f));
                        } catch (IOException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                    request.multipart = m;
                } else {
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_BLOCK_STAT: {
                if(isRequest) {
                    urlStr = local + version + "block/stat?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            // 'ipfs object' is a plumbing command used to manipulate DAG objects directly. {Object} is a subset of {Block}
            case OPERATION_OBJECT_PUT: {
                if(isRequest) {
                    urlStr = local + version + "object/put?stream-channels=true";
                    Multipart m = new Multipart(encoding);
                    for (byte[] f : request.dataBytesList) {
                        try {
                            m.addFilePart("file", new ByteArrayWrapper(f));
                        } catch (IOException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                    request.multipart = m;
                } else {
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_OBJECT_PUT_ENCODED: {
                if(isRequest) {
                    urlStr = local + version + "object/put?stream-channels=true&encoding=" + request.encoding;
                    Multipart m = new Multipart(encoding);
                    for (byte[] f : request.dataBytesList) {
                        try {
                            m.addFilePart("file", new ByteArrayWrapper(f));
                        } catch (IOException e1) {
                            e1.printStackTrace(); // TODO: Return error report instead
                        }
                    }
                    request.multipart = m;
                } else {
                    List<MerkleNode> merkleNodes = new ArrayList<>();
                    List<Object> objects = JSONParser.parseStream(contentStr);
                    for(Object object : objects) {
                        merkleNodes.add(MerkleNode.fromJSON(object));
                    }
                    response.merkleNodes = merkleNodes;
                }
                break;
            }
            case OPERATION_OBJECT_GET: {
                if(isRequest) {
                    urlStr = local + version + "object/get?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    Map json  = (Map)JSONParser.parse(contentStr);
                    json.put("Hash",request.hash.toString());
                    response.merkleNode = MerkleNode.fromJSON(json);
                }
                break;
            }
            case OPERATION_OBJECT_LINKS: {
                if(isRequest) {
                    urlStr = local + version + "object/links?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    response.merkleNode = MerkleNode.fromJSON(JSONParser.parse(contentStr));
                }
                break;
            }
            case OPERATION_OBJECT_STAT: {
                if(isRequest) {
                    urlStr = local + version + "object/stat?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_OBJECT_DATA: {
                if(isRequest) {
                    urlStr = local + version + "object/data?stream-channels=true&arg=" + request.hash.toString();
                } else {
                    response.resultBytes = contentBytes;
                }
                break;
            }
            case OPERATION_OBJECT_NEW: {
                if(isRequest) {
                    if(request.template != null && !ObjectTemplates.contains(request.template)) {
                        System.out.println("Unrecognised template: "+request.template);
                        // TODO: Return error report instead
                        return;
                    }
                    urlStr = local + version + "object/new?stream-channels=true"
                            + (request.template != null ? "&arg=" + request.template : "");
                } else {
                    response.merkleNode = MerkleNode.fromJSON(JSONParser.parse(contentStr));
                }
                break;
            }
            case OPERATION_OBJECT_PATCH: {
                if(isRequest) {
                    if(!ObjectPatchTypes.contains(request.command)) {
                        System.out.println("Illegal Object.patch command type: " + request.command);
                        // TODO: Return error report instead
                        return;
                    }
                    urlStr = local + version + "object/patch/" + request.command + "?arg=" + request.hash.toString();
                    if(request.name != null) urlStr += "&arg=" + request.name;
                    if(request.target != null) urlStr += "&arg=" + request.target.toString();
                    switch (request.command) {
                        case "add-link":
                            if (request.target == null) {
                                System.out.println("add-link requires name and target!");
                                // TODO: Return error report instead
                                return;
                            }
                        case "rm-link":
                            if (request.name == null) {
                                System.out.println("link name is required!");
                                // TODO: Return error report instead
                                return;
                            }
                        case "set-data":
                        case "append-data":
                            if (request.dataBytes == null) {
                                System.out.println("set-data requires data!");
                                System.out.println("link name is required!");
                                // TODO: Return error report instead
                                return;
                            }
                            urlStr += "&stream-channels=true";
                            Multipart m = new Multipart(encoding);
                            try {
                                m.addFilePart("file", new ByteArrayWrapper(request.dataBytes));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                // TODO: Return error report instead
                                return;
                            }
                            request.multipart = m;
                        default:
                            throw new IllegalStateException("Unimplemented");
                    }
                } else {
                    response.merkleNode = MerkleNode.fromJSON(JSONParser.parse(contentStr));
                }
                break;
            }
            case OPERATION_NAME_PUBLISH: {
                if(isRequest) {
                    urlStr = local + version + "name/publish?arg="
                            + (request.id == null ? "" : request.id + "&arg=")
                            + "/ipfs/" + request.hash.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_NAME_RESOLVE: {
                if(isRequest) {
                    urlStr = local + version + "name/resolve?arg=" + request.hash.toString();
                } else {
                    response.resultString = (String)((Map)JSONParser.parse(contentStr)).get("Path");
                }
                break;
            }
            case OPERATION_DHT_FINDPROVS: {
                if(isRequest) {
                    urlStr = local + version + "dht/findprovs?arg=" + request.hash.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DHT_QUERY: {
                if(isRequest) {
                    urlStr = local + version + "dht/query?arg=" + request.addr.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DHT_FINDPEER: {
                if(isRequest) {
                    urlStr = local + version + "dht/findpeer?arg=" + request.addr.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DHT_GET: {
                if(isRequest) {
                    urlStr = local + version + "dht/get?arg=" + request.hash.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DHT_PUT: {
                if(isRequest) {
                    urlStr = local + version + "dht/put?arg=" + request.key + "&arg=" + request.value;
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_FILE_LIST: {
                if(isRequest) {
                    urlStr = local + version + "file/ls?arg=" + request.path;
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            // Network commands
            case OPERATION_BOOTSTRAP_LIST_PEERS: {
                if(isRequest) {
                    urlStr = local + version + "bootstrap/";
                } else {
                    List<MultiAddress> multiAddresses = new ArrayList<>();
                    List<Object> peers = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Peers");
                    for(Object peer : peers) {
                        multiAddresses.add(new MultiAddress((String)peer));
                    }
                    response.multiAddresses = multiAddresses;
                }
                break;
            }
            case OPERATION_ADD_PEER: {
                if(isRequest) {
                    urlStr = local + version + "bootstrap/add?arg="+request.addr.toString();
                } else {
                    List<MultiAddress> multiAddresses = new ArrayList<>();
                    List<Object> peers = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Peers");
                    for(Object peer : peers) {
                        multiAddresses.add(new MultiAddress((String)peer));
                    }
                    response.multiAddresses = multiAddresses;
                }
                break;
            }
            case OPERATION_RM_PEER: {
                if(isRequest) {
                    urlStr = local + version + "boostrap/rm?"
                            + (request.all ? "all=true":"")
                            + "&arg=" + request.addr.toString();
                } else {
                    List<MultiAddress> multiAddresses = new ArrayList<>();
                    List<Object> peers = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Peers");
                    for(Object peer : peers) {
                        multiAddresses.add(new MultiAddress((String)peer));
                    }
                    response.multiAddresses = multiAddresses;
                }
                break;
            }
            // ipfs swarm is a tool to manipulate the network swarm. The swarm is the
            // component that opens, listens for, and maintains connections to other
            // ipfs peers in the internet.
            case OPERATION_SWARM_LIST_PEERS: {
                if(isRequest) {
                    urlStr = local + version + "swarm/peers?stream-channels=true";
                } else {
                    List<MultiAddress> multiAddresses = new ArrayList<>();
                    List<Object> peers = (List<Object>)((Map)JSONParser.parse(contentStr)).get("Strings");
                    for(Object peer : peers) {
                        multiAddresses.add(new MultiAddress((String)peer));
                    }
                    response.multiAddresses = multiAddresses;
                }
                break;
            }
            case OPERATION_SWARM_ADDRS: {
                if(isRequest) {
                    urlStr = local + version + "swarm/addres?stream-channels=true";
                } else {
                    response.resultMap = (Map)((Map)JSONParser.parse(contentStr)).get("Addrs");
                }
                break;
            }
            case OPERATION_SWARM_CONNECT: {
                if(isRequest) {
                    urlStr = local + version + "swarm/connect?arg="+request.multiAddr.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_SWARM_DISCONNECT: {
                if(isRequest) {
                    urlStr = local + version + "swarm/disconnect?arg="+request.multiAddr.toString();
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_DIAG_NET: {
                if(isRequest) {
                    urlStr = local + version + "diag/net?stream-channels=true";
                } else {
                    response.resultString = contentStr;
                }
                break;
            }
            case OPERATION_PING: {
                if(isRequest) {
                    urlStr = local + version + "ping/" + request.targetStr;
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_ID: {
                if(isRequest) {
                    urlStr = local + version + "id/" + request.targetStr;
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_STATS_BW: {
                if(isRequest) {
                    urlStr = local + version + "stats/bw";
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_VERSION: {
                if(isRequest) {
                    urlStr = local + version + "version";
                } else {
                    response.resultString = (String)((Map)JSONParser.parse(contentStr)).get("Version");
                }
                break;
            }
            case OPERATION_COMMANDS: {
                if(isRequest) {
                    urlStr = local + version + "commands";
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_LOG_TAIL: {
                if(isRequest) {
                    urlStr = local + version + "log/tail";
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_CONFIG_SHOW: {
                if(isRequest) {
                    urlStr = local + version + "config/show";
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_CONFIG_REPLACE: {
                if(isRequest) {
                    urlStr = local + version + "config/replace?stream-channels=true";
                    Multipart m = new Multipart(encoding);
                    try {
                        m.addFilePart("file",request.file);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        // TODO: Return error report instead
                        return;
                    }
                    request.multipart = m;
                } else {
                    // Do Nothing for now
                    System.out.println("config/replace called successfully.");
                }
                break;
            }
            case OPERATION_CONFIG_GET: {
                if(isRequest) {
                    urlStr = local + version + "config?arg=" + request.key;
                } else {
                    response.resultString = (String)((Map)JSONParser.parse(contentStr)).get("Value");
                }
                break;
            }
            case OPERATION_CONFIG_SET: {
                if(isRequest) {
                    urlStr = local + version
                            + "config?arg=" + request.key
                            + "&arg="+request.value;
                } else {
                    response.resultMap = (Map)JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_UPDATE: {
                if(isRequest) {
                    urlStr = local + version + "update";
                } else {
                    response.resultObject = JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_UPDATE_CHECK: {
                if(isRequest) {
                    urlStr = local + version + "update/check";
                } else {
                    response.resultObject = JSONParser.parse(contentStr);
                }
                break;
            }
            case OPERATION_UPDATE_LOG: {
                if(isRequest) {
                    urlStr = local + version + "update/log";
                } else {
                    response.resultObject = JSONParser.parse(contentStr);
                }
                break;
            }
            default: deadLetter(e);
        }
        try {
            e.setURL(new URL(urlStr));

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            // TODO: return error message
            return;
        }
        if(isRequest && request.multipart != null) {
            e.setMultipart(request.multipart);
        }
        DLC.addRoute(IPFSService.class, IPFSService.OPERATION_PACK,e);
        DLC.addRoute(SensorsService.class, SensorsService.OPERATION_SEND,e);
    }

    private String getActiveGateway() {
        // TODO: ClearnetSensor only works with this URL and HTTP for now
        // TODO: need to update SSL support before adding other HTTPS urls
        String activeGateway = "https://ipfs.io/ipfs/:hash";
//        if(useTor) {
//            for (String torGateway : torGateways.keySet()) {
//                if (torGateways.get(torGateway) == GatewayStatus.Active) {
//                    activeGateway = torGateway;break;
//                }
//            }
//        } else {
//            for (String clearnetGateway : clearnetGateways.keySet()) {
//                if (clearnetGateways.get(clearnetGateway) == GatewayStatus.Active) {
//                    activeGateway = clearnetGateway;break;
//                }
//            }
//        }
        return activeGateway; // default
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(IPFSService.class.getSimpleName()+": starting...");
        try {
            config = Config.loadFromClasspath("ipfs.config", properties);

            this.local = config.getProperty(PROP_IPFS_LOCAL);

            String encoding = config.getProperty(PROP_IPFS_ENCODING);
            if(encoding != null) {
                this.encoding = encoding;
            }

            String version = config.getProperty(PROP_IPFS_VERSION);
            if(version != null) {
                this.version = version;
            }

            String gatewaysListUrl = config.getProperty(PROP_GATEWAYS_LIST);
            if(gatewaysListUrl != null)
                this.gatewaysListUrl = gatewaysListUrl;

            String useTorString = config.getProperty(PROP_USE_TOR);
            if(useTorString != null) {
                this.useTor = Boolean.getBoolean(useTorString);
            }

            String torGatewaysString = config.getProperty(PROP_TOR_GATEWAYS);
            if(torGatewaysString != null) {
                List<String> torGateways = Arrays.asList(torGatewaysString.split(","));
                for(String gateway : torGateways) {
                    this.torGateways.put(gateway, GatewayStatus.Unknown);
                }
            }

            String clearnetGatewaysString = config.getProperty(PROP_CLEARNET_GATEWAYS);
            if(clearnetGatewaysString != null) {
                List<String> clearGateways = Arrays.asList(clearnetGatewaysString.split(","));
                for(String gateway : clearGateways) {
                    this.clearnetGateways.put(gateway, GatewayStatus.Unknown);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        System.out.println(IPFSService.class.getSimpleName()+": started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        System.out.println(IPFSService.class.getSimpleName()+": stopping...");

        System.out.println(IPFSService.class.getSimpleName()+": stopped.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
