package io.onemfive.core.sensors.i2p.bote;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.sensors.Sensor;
import io.onemfive.core.sensors.i2p.I2PRouterUtil;
import io.onemfive.core.sensors.i2p.bote.email.Attachment;
import io.onemfive.core.sensors.i2p.bote.email.Email;
import io.onemfive.core.sensors.i2p.bote.email.EmailIdentity;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.folder.EmailFolder;
import io.onemfive.core.sensors.i2p.bote.folder.NewEmailListener;
import io.onemfive.core.sensors.i2p.bote.network.NetworkStatusListener;
import io.onemfive.core.sensors.i2p.bote.util.BoteHelper;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Provides an API for I2P Bote Router.
 * By default, looks for a running I2P Bote instance.
 * If discovered and is configured appropriately, will use it.
 * If discovered and is not configured appropriately, will launch new configured instance.
 * If not found to be installed, will send a message to end user that they need to install I2P Bote.
 *
 * @author objectorange
 */
public class I2PBoteSensor implements Sensor, NetworkStatusListener, NewEmailListener {

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

    @Override
    public boolean send(Envelope envelope) {
        DocumentMessage message = (DocumentMessage)envelope.getMessage();
        Map<String,Object> m = message.data.get(0);
        Email email = new Email(i2PBote.getConfiguration().getIncludeSentTime());
//        try {
            if(m.containsKey(io.onemfive.data.Email.class.getName())) {
                // Only 1M5 Emails for now
                io.onemfive.data.Email m5 = (io.onemfive.data.Email)m.get(io.onemfive.data.Email.class.getName());
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
            }

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
        System.out.println(statusText);
    }

    @Override
    public void emailReceived(String messageId) {
//        NotificationManager nm = (NotificationManager) getSystemService(
//                Context.NOTIFICATION_SERVICE);
//
//        NotificationCompat.Builder b = new NotificationCompat.Builder(this)
//                .setAutoCancel(true)
//                .setSmallIcon(R.drawable.ic_notif)
//                .setDefaults(Notification.DEFAULT_ALL);
        try {
            EmailFolder inbox = I2PBote.getInstance().getInbox();

            // Set the new email as \Recent
            inbox.setRecent(messageId, true);

            // Now display/update notification with all \Recent emails
            List<Email> newEmails = BoteHelper.getRecentEmails(inbox);
            int numNew = newEmails.size();
            switch (numNew) {
                case 0:
//                    nm.cancel(NOTIF_ID_NEW_EMAIL);
                    return;

                case 1:
                    Email email = newEmails.get(0);

                    String fromAddress = email.getOneFromAddress();
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
                    break;

                default:
//                    b.setContentTitle(getResources().getQuantityString(
//                            R.plurals.n_new_emails, numNew, numNew));

                    HashSet<Address> recipients = new HashSet<>();
                    String bigText = "";
                    for (Email ne : newEmails) {
                        recipients.add(BoteHelper.getOneLocalRecipient(ne));
                        bigText += BoteHelper.getNameAndShortDestination(
                                ne.getOneFromAddress());
                        bigText += ": " + ne.getSubject() + "\n";
                    }
//                    b.setContentText(BoteHelper.joinAddressNames(recipients));
//                    b.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
//
//                    Intent eli = new Intent(this, EmailListActivity.class);
//                    eli.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    PendingIntent peli = PendingIntent.getActivity(this, 0, eli, PendingIntent.FLAG_UPDATE_CURRENT);
//                    b.setContentIntent(peli);
            }
        } catch (PasswordException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        nm.notify(NOTIF_ID_NEW_EMAIL, b.build());
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        status = Status.STARTING;
        router = I2PRouterUtil.getGlobalI2PRouter(properties, true);
        i2PBote = I2PBote.getInstance();
        i2PBote.startUp();
        i2PBote.addNewEmailListener(this);
        i2PBote.addNetworkStatusListener(this);
        status = Status.RUNNING;
        LOG.info("Started.");
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
