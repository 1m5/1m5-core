package io.onemfive.core.did;

import io.onemfive.core.*;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.data.DID;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Decentralized IDentifier (DID) Service
 *
 * Working to implement a Web Of Trust - Self Sovereign Identity
 *
 * Bill of Rights: https://github.com/WebOfTrustInfo/self-sovereign-identity/blob/master/self-sovereign-identity-bill-of-rights.md
 * How we plan on supporting these Bill of Rights:
 *
 * <h2>1. Individuals must be able to establish their existence as a unified identity online and in the physical world</h2>
 * <p>
 *     A unified identity requires that people not only have an online presence, but that presence must function seamlessly
 *     across both online and real-world environments. One unified identity for all spheres of life.
 * </p>
 * <p>
 *     One unified identity can come about through individual projects by supporting common standards.
 *     1M5 will only use standard algorithms well supported globally and any standard interfaces that become widely adopted
 *     so long as those interfaces come from open source and free efforts.
 * </p>
 * <ul>
 *     <li>AES symmetric keys for encrypting identity keys and as session keys</li>
 *     <li>SHA256 hashing for signatures and other integrity verifications</li>
 *     <li>ElGamal for asymmetric keys</li>
 * </ul>
 * <h2>2. Individuals must have the tools to access and control their identities</h2>
 * <p>
 *     Self-sovereign identity holders must be able to easily retrieve identity attributes and verified claims as well
 *     as any metadata that has been generated in the process of transactions. There can be no personally identifiable
 *     information (PII) data that is hidden from the identity holder. This includes management, updating or changing
 *     identity attributes, and keeping private what they choose.
 * </p>
 * <ul>
 *     <li>Service interfaces for managing identities that can be called by user interface based applications.</li>
 * </ul>
 * <h2>3. The platforms and protocols on which self-sovereign identities are built, must be open and transparent</h2>
 * <p>
 *     This refers to how the platforms and protocols are governed, including how they are managed and updated.
 *     They should be open-source, well-known, and as independent as possible of any particular architecture;
 *     anyone should be able to examine how they work.
 * </p>
 * <ul>
 *     <li>All code in the 1M5 project is GPLv3 open source.</li>
 * </ul>
 * <h2>4. Users must have the right to participate in the governance of their identity infrastructure</h2>
 * <p>
 *     The platform, protocols on which self-sovereign identities are built, must be governed by identity holders.
 *     By definition, if the platform is governed by a private entity or limited set of participants, the Identity holder
 *     is not in control of the future of their identity.
 * </p>
 * <ul>
 *     <li>1M5 wil be governed by members and membership will be open to those using it through their public keys (identities)</li>
 *     <li>Governance within the application will be supported in the future by members, especially with the implementation of V4D.io.</li>
 * </ul>
 * <h2>5. Identities must exist for the life of the identity holder</h2>
 * <p>
 *     While the platform and protocols evolve, each singular identity must remain intact. This must not contradict a
 *     "right to be forgotten"; a user should be able to dispose of an identity if he or she wishes and claims should
 *     be modified or removed as appropriate over time. To do this requires a firm separation between an identity and
 *     its claims: they can't be tied forever.
 * </p>
 * <ul>
 *     <li>1M5 is a decentralized autonomous organization in that it is not registered in any jurisdiction and thus will exist so long as there is membership.</li>
 *     <li>All keys and the data they have access to can be deleted from the system at any time</li>
 *     <li>Data provided to another party is forever un-controllable as a copy is provided to the other party</li>
 *     <li>Data provided to another party at a given time does not mean that any future data will be given unless explicitly given permission</li>
 *     <li>Any recurring data access to another party can be immediately canceled at any time.</li>
 * </ul>
 * <h2>6. Identities must be portable</h2>
 * <p>
 *     Identity attributes and verified claims must be controlled personally and be transportable and interoperable
 *     as desired. Government entities, companies and other individuals can come and go. So it is essential that
 *     identity holders can move their identity data to other blockchains or platforms to ensure that they alone
 *     control their identity.
 * </p>
 * <ul>
 *     <li>Using common standards today and as the industry evolves while ensuring keys can be imported, exported, and mapped to new technologies as they evolve</li>
 * </ul>
 * <h2>7. Identities must be interoperable</h2>
 * <p>
 *     Identity holders must be able to us their identities in all facets of their lives. So any identity platform
 *     or protocol must function across geographical, political and commercial jurisdictions. Identities should be as
 *     widely usable as possible. Ultimately, identities are of little value if they only work in niches.
 * </p>
 * <ul>
 *     <li>Common interfaces will be supported so long as they do not violate free and open source systems.</li>
 * </ul>
 * <h2>8. Individuals must consent to the use of their identity</h2>
 * <p>
 *     The point of having an identity is that you can use it to participate in mutually beneficial
 *     transactions — whether personal or commercial. This requires that some amount of personal information
 *     needs to be shared. However, any sharing of personal data must require the absolute consent of the
 *     user — even if third parties have a record of previously verified claims. For every transaction associate
 *     with a claim, the identity holder must deliberately consent to its use.
 * </p>
 * <ul>
 *     <li>Only public keys can be given out.</li>
 *     <li>If a 'power of attorney' type action is desired (such as for the elderly), it can be set up to support that without giving away private keys. (Expected in future development)</li>
 * </ul>
 * <h2>9. Disclosure of verified claims must be minimized</h2>
 * <p>
 *     For every transaction, only the minimum amount of personally identifiable information should be required
 *     and shared. If an identity holder wants to enable an age-related commercial transaction, e.g. buy alcohol,
 *     the only verified claim that needs to be share is whether they are over 21. There is not need to share actual age,
 *     street address, height, weight, etc.
 * </p>
 * <ul>
 *     <li>Access can be given to data at a very granular level explicitly approved by the member.</li>
 *     <li>No integrations with legal systems will be tolerated. Laws are made by one group to control other groups behaviors and normally found to be unethical. Although data can be shown to 3rd parties if the member wishes as in the example with being old enough to purchase alcohol in a jurisdiction requiring proof of age.</li>
 *     <li>The only rules to be followed within 1M5 is ethics and it's defined to be the non-aggression principle / voluntary relationships (voluntaryism).</li>
 * </ul>
 * <h2>10. The rights of identity holders must supersede any other platform or ecosystem entities</h2>
 * <p>
 *     If a conflict arises between the needs of the platform or entities engaging with identity holders, the
 *     governance must be designed to err on the side of preserving these rights for identity holder over the
 *     needs of the protocols, platform or network. To ensure this, identity authentication must be decentralized,
 *     independent, and free of censorship.
 * </p>
 * <ul>
 *     <li>Decentralized: 1M5 only uses P2P open source free software systems</li>
 *     <li>Independent: 1M5 is a DAO with no jurisdiction oversight, only member oversight</li>
 *     <li>Free of Censorship: 1M5's bread and butter - anonymous highly censorship resistant communications with strong at-rest data encryption</li>
 * </ul>
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DIDService.class.getName());

    public static final String OPERATION_VERIFY = "VERIFY";
    public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_LOAD = "LOAD";
    public static final String OPERATION_AUTHN_LOAD = "AUTHN_LOAD";
    public static final String OPERATION_HASH = "HASH";
    public static final String OPERATION_VERIFY_HASH = "VERIFY_HASH";

    public static final String MESSAGE_DIGEST_SHA1 = "SHA1";
    public static final String MESSAGE_DIGEST_SHA256 = "SHA256";
    public static final String MESSAGE_DIGEST_SHA384 = "SHA384";
    public static final String MESSAEG_DIGEST_SHA512 = "SHA512";

    private Map<String,DID> contacts;

    public DIDService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        handleAll(e);
    }

    private void handleAll(Envelope e) {
        Route route = e.getRoute();
        switch(route.getOperation()) {
            case OPERATION_VERIFY: {verify(e);break;}
            case OPERATION_AUTHENTICATE: {authenticate(e);break;}
            case OPERATION_CREATE: {create(e);break;}
            case OPERATION_LOAD: {load(e);break;}
            case OPERATION_AUTHN_LOAD: {authnLoad(e);break;}
            case OPERATION_HASH: {
                HashRequest r = (HashRequest)DLC.getData(HashRequest.class,e);
                hash(r);
                break;
            }
            case OPERATION_VERIFY_HASH:{
                VerifyHashRequest r = (VerifyHashRequest)DLC.getData(VerifyHashRequest.class,e);
                verifyHash(r);
                break;
            }
            default: deadLetter(e); // Operation not supported
        }
    }

    private void verify(Envelope e) {
        LOG.info("Received verify DID request.");
        DID did = e.getDID();
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())) {
            didLoaded.setVerified(true);
            e.setDID(didLoaded);
            LOG.info("DID verification successful.");
        } else {
            did.setVerified(false);
            LOG.info("DID verification unsuccessful.");
        }
    }

    /**
     * Creates and returns identity key using master key for provided alias if one does not exist.
     * If master key is not present, one will be created by the Key Ring Service.
     * @param e
     */
    private void create(Envelope e) {
        LOG.info("Received create DID request.");
        DID did = e.getDID();
        // make sure we don't already have a key
        if(contacts.get(did.getAlias()) == null) {

        }
//        DID didCreated = infoVault.getDidDAO().createDID(did.getAlias(), did.getPassphrase());
//        didCreated.setAuthenticated(true);
//        e.setDID(didCreated);
    }

    /**
     * Authenticates passphrase
     * @param e
     */
    private void authenticate(Envelope e) {
        LOG.info("Received authn DID request.");
        DID did = e.getDID();
        DID didLoaded = infoVault.getDidDAO().load(did.getAlias());
        // TODO: Replace with I2PBote example below
        if(didLoaded != null && did.getAlias() != null && did.getAlias().equals(didLoaded.getAlias())
                && did.getPassphrase() != null && did.getPassphrase().equals(didLoaded.getPassphrase())) {
            didLoaded.setAuthenticated(true);
            e.setDID(didLoaded);
        } else {
            did.setAuthenticated(false);
        }
    }

    private void load(Envelope e) {
        LOG.info("Ensure I2P identities are present in DID. Request them from Sensor Service using Very High Sensitivity.");
        // Very High Sensitivity selects I2P by default
        e.setSensitivity(Envelope.Sensitivity.VERYHIGH);
        DLC.addRoute(SensorsService.class, SensorsService.OPERATION_GET_KEYS,e);
    }

    private void authnLoad(Envelope e) {
        verify(e);
        if(!e.getDID().getVerified()) {
            create(e);
        } else {
            authenticate(e);
        }
        if(e.getDID().getAuthenticated()) {
            load(e);
        }
    }

    private void hash(HashRequest r) {
        try {
            MessageDigest md = MessageDigest.getInstance(r.hashAlgorithm);
            r.hash = md.digest(r.contentToHash);
        } catch (NoSuchAlgorithmException e1) {
            r.exception = e1;
        }
    }

    private void verifyHash(VerifyHashRequest r) {
        HashRequest hr = new HashRequest();
        hr.contentToHash = r.content;
        hr.hashAlgorithm = r.hashAlgorithm;
        hash(hr);
        r.isAMatch = hr.exception == null && hr.hash != null && new String(hr.hash).equals(new String(r.hashToVerify));
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        contacts = new HashMap<>();

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
