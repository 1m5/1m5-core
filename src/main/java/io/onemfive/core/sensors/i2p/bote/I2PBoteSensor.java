package io.onemfive.core.sensors.i2p.bote;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.sensors.BaseSensor;
import io.onemfive.core.sensors.Sensor;
import io.onemfive.core.sensors.SensorsService;
import io.onemfive.core.sensors.i2p.I2PRouterUtil;
import io.onemfive.core.sensors.i2p.bote.crypto.PublicKeyPair;
import io.onemfive.core.sensors.i2p.bote.email.*;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordCacheListener;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.folder.EmailFolder;
import io.onemfive.core.sensors.i2p.bote.folder.NewEmailListener;
import io.onemfive.core.sensors.i2p.bote.network.NetworkStatusListener;
import io.onemfive.core.sensors.i2p.bote.status.ChangeIdentityStatus;
import io.onemfive.core.sensors.i2p.bote.status.StatusListener;
import io.onemfive.core.sensors.i2p.bote.util.BoteHelper;
import io.onemfive.core.sensors.i2p.bote.util.GeneralHelper;
import io.onemfive.data.DID;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import io.onemfive.data.util.DLC;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.logging.Logger;

/**
 * Provides an API for I2P Bote Router as a Sensor.
 * I2P Bote in 1M5 can be used as Message-Oriented-Middleware (MOM).
 *
 * @author objectorange
 */
public class I2PBoteSensor extends BaseSensor implements NetworkStatusListener, NewEmailListener {

    private static final Logger LOG = Logger.getLogger(I2PBoteSensor.class.getName());

    public enum Status {
        // These states persist even if it died.
        INIT, WAITING, STARTING, RUNNING, ACTIVE,
        // button, don't kill service when paused, stay in PAUSED
        PAUSING, PAUSED,
        //
        UNPAUSING,
        // button, kill service when stopped
        STOPPING, STOPPED,
        // Stopped by listener (no network), next: WAITING (spin waiting for network)
        NETWORK_STOPPING, NETWORK_STOPPED,
        // button,
        GRACEFUL_SHUTDOWN
    }

    private static Router router;
    private Status status = Status.INIT;
    private I2PBote i2PBote;

    public I2PBoteSensor(SensorsService sensorsService) {
        super(sensorsService);
    }

    @Override
    public boolean send(Envelope envelope) {
        DocumentMessage message = (DocumentMessage)envelope.getMessage();
        Map<String,Object> m = message.data.get(0);
        Email email = new Email(i2PBote.getConfiguration().getIncludeSentTime());

//        try {

                // Set sender
//                EmailIdentity sender = (EmailIdentity) mSpinner.getSelectedItem();
//                InternetAddress ia = new InternetAddress(
//                        sender == null ? "Anonymous" :
//                                BoteHelper.getNameAndDestination(sender.getKey()));
//                email.setFrom(ia);
                // We must continue to set "Sender:" even with only one mailbox
                // in "From:", which is against RFC 2822 but required for older
                // Bote versions to see a sender (and validate the signature).
//                email.setSender(ia);

//                for (Object obj : mTo.getObjects()) {
//                    Person person = (Person) obj;
//                    email.addRecipient(Message.RecipientType.TO, new InternetAddress(
//                            person.getAddress(), person.getName()));
//                }
//                if (mMoreVisible) {
//                    for (Object obj : mCc.getObjects()) {
//                        Person person = (Person) obj;
//                        email.addRecipient(Message.RecipientType.CC, new InternetAddress(
//                                person.getAddress(), person.getName()));
//                    }
//                    for (Object obj : mBcc.getObjects()) {
//                        Person person = (Person) obj;
//                        email.addRecipient(Message.RecipientType.BCC, new InternetAddress(
//                                person.getAddress(), person.getName()));
//                    }
//                }

                // Check that we have someone to send to
//                Address[] rcpts = email.getAllRecipients();
//                if (rcpts == null || rcpts.length == 0) {
                    // No recipients
//                    mTo.setError(getActivity().getString(R.string.add_one_recipient));
//                    mTo.requestFocus();
//                    return false;
//                } else {
//                    mTo.setError(null);
//                }

//                email.setSubject(mSubject.getText().toString(), "UTF-8");

                // Extract the attachments
//                List<Attachment> attachments = new ArrayList<>();
//                for (int i = 0; i < mAttachments.getChildCount(); i++) {
//                    View v = mAttachments.getChildAt(i);
                    // Warning views don't have tags set
//                    if (v.getTag() != null)
//                        attachments.add((Attachment) v.getTag());
//                }

                // Set the text and add attachments
//                email.setContent(mContent.getText().toString(), attachments);

                // Cache the fact that we sent this email
//                BoteHelper.setEmailSent(email, true);

                // Send the email
//                i2PBote.sendEmail(email);

                // Clean up attachments
//            for (Attachment attachment : attachments) {
//                if (!attachment.clean())
//                    Log.e(Constants.ANDROID_LOG_TAG, "Can't clean up attachment: <" + attachment + ">");
//            }

//            return true;
//        } catch (PasswordException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (AddressException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (MessagingException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        return false;
    }

    @Override
    public boolean reply(Envelope envelope) {
        return false;
    }

    public void getKeys(Envelope e) {
        DID did = e.getDID();
        LOG.info("Creating new identities: is did null="+Boolean.toString(e.getDID() == null));
        Identities identities = i2PBote.getIdentities();
        EmailIdentity emailIdentity = null;
        try {
            emailIdentity = identities.getDefault();
            if(emailIdentity != null) {
                LOG.info("Default Identity found.");
            } else {
                LOG.info("Default Identity not found; must create.");
            }
        } catch (PasswordException e1) {
            LOG.warning(e1.getLocalizedMessage());
        } catch (IOException e1) {
            LOG.warning(e1.getLocalizedMessage());
        } catch (GeneralSecurityException e1) {
            LOG.warning(e1.getLocalizedMessage());
        }
        if(emailIdentity == null) {
            // No Default Identity therefore create one
            StatusListener<ChangeIdentityStatus> lsnr = new StatusListener<ChangeIdentityStatus>() {
                @Override
                public void updateStatus(ChangeIdentityStatus changeIdentityStatus, String... args) {
                    LOG.info(changeIdentityStatus.toString());
                }
            };
            /**
             * Args:
             * 1 = new?
             * 2 = Crypto IDs:
             *      1 = ElGamal-2048 / DSA-1024
             *      2 = ECDH-256 / ECDSA-256
             *      3 = ECDH-521 / ECDSA-521
             *      4 = NTRUEncrypt-1087 / GMSS-512
             * 3,4 = null
             * 5 = Alias/Public Name
             * 6-9 = null
             * 10 = default?
             * 11 = StatusListener
             */
            try {
                LOG.info("Creating default identities with alias: "+did.getAlias());
                GeneralHelper.createOrModifyIdentity(true, 1, null, null, did.getAlias(), null, null, null, null, true, lsnr);
                emailIdentity = identities.getDefault();
                LOG.info("Was new default identity created: "+Boolean.toString(emailIdentity != null));
                LOG.info("Saving Identities...");
                identities.save();
            } catch (GeneralSecurityException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (PasswordException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (IOException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } catch (IllegalDestinationParametersException e1) {
                LOG.warning(e1.getLocalizedMessage());
            }
        }
        if(emailIdentity != null && emailIdentity.getPublicName() != null && emailIdentity.getPublicName().equals(did.getAlias())) {
            LOG.info("Building up DID for alias "+did.getAlias()+"...");
            PublicKey publicEncryptionKey = emailIdentity.getPublicEncryptionKey();
            PrivateKey privateEncryptionKey = emailIdentity.getPrivateEncryptionKey();
            KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);
            did.addEncryptionKeys(DID.Provider.I2P, did.getAlias(), encryptionKeyPair);

            PublicKey publicSigningKey = emailIdentity.getPublicSigningKey();
            PrivateKey privateSigningKey = emailIdentity.getPrivateSigningKey();
            KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
            did.addIdentity(DID.Provider.I2P, did.getAlias(), signingKeyPair);

            PublicKeyPair publicKeyPair = new PublicKeyPair(publicEncryptionKey, publicSigningKey);
            try {
                did.addEncodedKey(emailIdentity.getCryptoImpl().toBase64(publicKeyPair));
                LOG.info("Base64 public key pair: "+did.getEncodedKey());
            } catch (GeneralSecurityException e1) {
                LOG.warning("GeneralSecurityException caught while converting public keys to base 64.");
            }
        }
    }

    @Override
    public void networkStatusChanged() {
        String statusText;
        switch (I2PBote.getInstance().getNetworkStatus()) {
            case DELAY:
                statusText = "Waiting for I2P Network...";
                break;
            case CONNECTING:
                statusText = "Connecting to I2P Network...";
                break;
            case CONNECTED:
                statusText = "Connected to I2P Network.";
                break;
            case ERROR:
                statusText = "Error connecting to I2P Network.";
                break;
            case NOT_STARTED:
            default:
                statusText = "Not connected to I2P Network.";
        }
        LOG.info(statusText);
    }

    @Override
    public void emailReceived(String messageId) {
        EmailFolder inbox = i2PBote.getInbox();
        try {
            // Set the new email as \Recent
            inbox.setRecent(messageId, true);

            // Now display/update notification with all \Recent emails
            List<Email> newEmails = BoteHelper.getRecentEmails(inbox);
            int numNew = newEmails.size();
            switch (numNew) {
                case 0: {
//                    nm.cancel(NOTIF_ID_NEW_EMAIL);
                    return;
                }
                case 1: {
                    Email email = newEmails.get(0);
                    // TODO: Begin unpacking email into Envelope and forwarding onto Service Bus via Sensors Service
//                    String fromAddress = email.getOneFromAddress();
                    Envelope envelope = (Envelope)email.getContent();
//                    Bitmap picture = BoteHelper.getPictureForAddress(fromAddress);
//                    if (picture != null)
//                        b.setLargeIcon(picture);
//                    else if (!email.isAnonymous()) {
//                        int width = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
//                        int height = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_height);
//                        b.setLargeIcon(BoteHelper.getIdenticonForAddress(fromAddress, width, height));
//                    } else
//                        b.setSmallIcon(R.drawable.ic_contact_picture);
//
//                    b.setContentTitle(BoteHelper.getNameAndShortDestination(
//                            fromAddress));
//                    b.setContentText(email.getSubject());
//
//                    Intent vei = new Intent(this, ViewEmailActivity.class);
//                    vei.putExtra(ViewEmailActivity.FOLDER_NAME, inbox.getName());
//                    vei.putExtra(ViewEmailActivity.MESSAGE_ID, email.getMessageID());
//                    vei.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    PendingIntent pvei = PendingIntent.getActivity(this, 0, vei, PendingIntent.FLAG_UPDATE_CURRENT);
//                    b.setContentIntent(pvei);
                    sensorsService.sendToBus(envelope);
                    break;
                }
                default: {
//                    b.setContentTitle(getResources().getQuantityString(
//                            R.plurals.n_new_emails, numNew, numNew));

//                    HashSet<Address> recipients = new HashSet<>();
//                    String bigText = "";
                    for (Email ne : newEmails) {
//                        recipients.add(BoteHelper.getOneLocalRecipient(ne));
//                        bigText += BoteHelper.getNameAndShortDestination(ne.getOneFromAddress());
//                        bigText += ": " + ne.getSubject() + "\n";
                        Envelope envelope = (Envelope)ne.getContent();
                        sensorsService.sendToBus(envelope);
                    }
//                    b.setContentText(BoteHelper.joinAddressNames(recipients));
//                    b.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
//
//                    Intent eli = new Intent(this, EmailListActivity.class);
//                    eli.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    PendingIntent peli = PendingIntent.getActivity(this, 0, eli, PendingIntent.FLAG_UPDATE_CURRENT);
//                    b.setContentIntent(peli);
                }
            }
        } catch (PasswordException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (MessagingException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (GeneralSecurityException e) {
            LOG.warning(e.getLocalizedMessage());
        }

//        nm.notify(NOTIF_ID_NEW_EMAIL, b.build());
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting I2P Bote version "+I2PBote.getAppVersion()+"...");
        status = Status.STARTING;
//        router = I2PRouterUtil.getGlobalI2PRouter(properties, true);
        i2PBote = I2PBote.getInstance();
        i2PBote.startUp();
        i2PBote.addNewEmailListener(this);
        i2PBote.addNetworkStatusListener(this);
        status = Status.RUNNING;
        LOG.info("I2P Bote Started.");
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");

        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        LOG.info("Gracefully shutting down...");

        LOG.info("Gracefully shutdown.");
        return true;
    }
}
