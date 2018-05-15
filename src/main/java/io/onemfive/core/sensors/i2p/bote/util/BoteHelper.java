package io.onemfive.core.sensors.i2p.bote.util;

import com.lambdaworks.codec.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Part;

import io.onemfive.core.sensors.i2p.bote.email.Email;
import io.onemfive.core.sensors.i2p.bote.email.EmailDestination;
import io.onemfive.core.sensors.i2p.bote.email.EmailIdentity;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.folder.EmailFolder;
import io.onemfive.core.sensors.i2p.bote.folder.Outbox.EmailStatus;
import io.onemfive.core.sensors.i2p.bote.packet.dht.Contact;
import io.onemfive.core.util.Log;

public class BoteHelper extends GeneralHelper {

    public static int getNumNewEmails(EmailFolder folder) throws PasswordException, GeneralSecurityException, IOException, MessagingException {
//        String selectedIdentityKey = ctx.getSharedPreferences(Constants.SHARED_PREFS, 0)
//                .getString(Constants.PREF_SELECTED_IDENTITY, null);
//        if (selectedIdentityKey == null)
            return folder.getNumNewEmails();

//        int numNew = 0;
//        for (Email email : BoteHelper.getEmails(folder, null, true)) {
//            if (email.getMetadata().isUnread()) {
//                if (BoteHelper.isSentEmail(email)) {
//                    String senderDest = BoteHelper.extractEmailDestination(email.getOneFromAddress());
//                    if (selectedIdentityKey.equals(senderDest))
//                        numNew++;
//                } else {
//                    for (Address recipient : email.getAllRecipients()) {
//                        String recipientDest = BoteHelper.extractEmailDestination(recipient.toString());
//                        if (selectedIdentityKey.equals(recipientDest)) {
//                            numNew++;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return numNew;
    }

    /**
     * Get the translated name of the folder.
     * Built-in folders are special-cased; other folders are created by the
     * user, so their name is already "translated".
     *
     * @param folder The folder.
     * @return The name of the folder.
     */
    public static String getFolderDisplayName(EmailFolder folder) {
        String name = folder.getName();
//        if ("inbox".equals(name))
//            return ctx.getResources().getString(R.string.folder_inbox);
//        else if ("outbox".equals(name))
//            return ctx.getResources().getString(R.string.folder_outbox);
//        else if ("sent".equals(name))
//            return ctx.getResources().getString(R.string.folder_sent);
//        else if ("trash".equals(name))
//            return ctx.getResources().getString(R.string.folder_trash);
//        else
            return name;
    }

    /**
     * Get the translated name of the folder with the number of
     * new messages it contains appended.
     *
     * @param folder The folder.
     * @return The name of the folder.
     * @throws PasswordException
     */
    public static String getFolderDisplayNameWithNew(EmailFolder folder) throws PasswordException, GeneralSecurityException, IOException, MessagingException {
        String displayName = getFolderDisplayName(folder);

        int numNew = getNumNewEmails(folder);
        if (numNew > 0)
            displayName = displayName + " (" + numNew + ")";

        return displayName;
    }

    public static String getDisplayAddress(String address) throws PasswordException, IOException, GeneralSecurityException, MessagingException {
        String fullAdr = getNameAndDestination(address);
        String emailDest = extractEmailDestination(fullAdr);
        String name = extractName(fullAdr);

        return (emailDest == null ? address
                : (name.isEmpty() ? emailDest.substring(0, 10)
                : name + " <" + emailDest.substring(0, 10) + "...>"));
    }

    private static final String PROPERTY_SENT = "sent";

    public static void setEmailSent(Email email, boolean isSent) {
        email.getMetadata().setProperty(PROPERTY_SENT, isSent ? "true" : "false");
    }

    /**
     * Determines if we sent this email, either anonymously or from a local identity.
     *
     * @param email The Email to query metadata for
     * @return true if we sent this email, false otherwise
     * @throws PasswordException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws MessagingException
     */
    public static boolean isSentEmail(Email email) throws PasswordException, IOException, GeneralSecurityException, MessagingException {
        boolean isSent;

        if (email.getMetadata().containsKey(PROPERTY_SENT)) {
            String sentStr = email.getMetadata().getProperty(PROPERTY_SENT);
            isSent = "true".equals(sentStr);
        } else {
            // Figure it out
            // Is the sender anonymous?
            if (email.isAnonymous()) {
                // Assume we sent it unless we are a recipient
                isSent = true;

                Address[] recipients = email.getAllRecipients();
                for (Address recipient : recipients) {
                    String toDest = EmailDestination.extractBase64Dest(recipient.toString());
                    if (toDest != null && getIdentity(toDest) != null) {
                        // We are a recipient
                        isSent = false;
                        break;
                    }
                }
            } else {
                // Are we the sender?
                String fromAddress = email.getOneFromAddress();
                String fromDest = EmailDestination.extractBase64Dest(fromAddress);
                isSent = (fromDest != null && getIdentity(fromDest) != null);
            }

            // Cache for next time
            setEmailSent(email, isSent);
        }

        return isSent;
    }

//    public static String getEmailStatusText(Context ctx, Email email, boolean full) {
//        Resources res = ctx.getResources();
//        EmailStatus emailStatus = getEmailStatus(email);
//        switch (emailStatus.getStatus()) {
//            case QUEUED:
//                return res.getString(R.string.queued);
//            case SENDING:
//                return res.getString(R.string.sending);
//            case SENT_TO:
//                if (full)
//                    return res.getString(R.string.sent_to,
//                            (Integer) emailStatus.getParam1(), (Integer) emailStatus.getParam2());
//                else
//                    return res.getString(R.string.sent_to_short,
//                            (Integer) emailStatus.getParam1(), (Integer) emailStatus.getParam2());
//            case EMAIL_SENT:
//                return res.getString(R.string.email_sent);
//            case GATEWAY_DISABLED:
//                return res.getString(R.string.gateway_disabled);
//            case NO_IDENTITY_MATCHES:
//                if (full)
//                    return res.getString(R.string.no_identity_matches,
//                            emailStatus.getParam1());
//            case INVALID_RECIPIENT:
//                if (full)
//                    return res.getString(R.string.invalid_recipient,
//                            emailStatus.getParam1());
//            case ERROR_CREATING_PACKETS:
//                if (full)
//                    return res.getString(R.string.error_creating_packets,
//                            emailStatus.getParam1());
//            case ERROR_SENDING:
//                if (full)
//                    return res.getString(R.string.error_sending,
//                            emailStatus.getParam1());
//            case ERROR_SAVING_METADATA:
//                if (full)
//                    return res.getString(R.string.error_saving_metadata,
//                            emailStatus.getParam1());
//            default:
//                // Short string for errors and unknown status
//                return res.getString(R.string.error);
//        }
//    }

    public static boolean isInbox(EmailFolder folder) {
        return isInbox(folder.getName());
    }

    public static boolean isInbox(String folderName) {
        return "Inbox".equalsIgnoreCase(folderName);
    }

    public static boolean isOutbox(EmailFolder folder) {
        return isOutbox(folder.getName());
    }

    public static boolean isOutbox(String folderName) {
        return "Outbox".equalsIgnoreCase(folderName);
    }

    public static boolean isTrash(EmailFolder folder) {
        return isTrash(folder.getName());
    }

    public static boolean isTrash(String folderName) {
        return "Trash".equalsIgnoreCase(folderName);
    }

    public static List<Email> getRecentEmails(EmailFolder folder) throws PasswordException, MessagingException {
        List<Email> emails = folder.getElements();
        Iterator<Email> iter = emails.iterator();
        while (iter.hasNext()) {
            Email email = iter.next();
            if (!email.isRecent())
                iter.remove();
        }
        return emails;
    }

    public interface RequestPasswordListener {
        public void onPasswordVerified();

        public void onPasswordCanceled();
    }

//    /**
//     * Request the password from the user, and try it.
//     */
//    public static void requestPassword(final Context context, final RequestPasswordListener listener) {
//        requestPassword(context, listener, null);
//    }

//    /**
//     * Request the password from the user, and try it.
//     *
//     * @param error is pre-filled in the dialog if not null.
//     */
//    public static void requestPassword(final Context context, final RequestPasswordListener listener, String error) {
//        LayoutInflater li = LayoutInflater.from(context);
//        View promptView = li.inflate(R.layout.dialog_password, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(promptView);
//
//        final EditText passwordInput = (EditText) promptView.findViewById(R.id.passwordInput);
//        if (error != null) {
//            TextView passwordError = (TextView) promptView.findViewById(R.id.passwordError);
//            passwordError.setText(error);
//            passwordError.setVisibility(View.VISIBLE);
//        }
//
//        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(passwordInput.getWindowToken(), 0);
//                dialog.dismiss();
//                new PasswordWaiter(context, listener).execute(passwordInput.getText().toString());
//            }
//        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//                if (listener != null)
//                    listener.onPasswordCanceled();
//            }
//        }).setCancelable(false);
//        AlertDialog passwordDialog = builder.create();
//        passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        passwordDialog.show();
//    }

//    private static class PasswordWaiter extends AsyncTask<String, Void, String> {
//        private final Context mContext;
//        private final ProgressDialog mDialog;
//        private final RequestPasswordListener mListener;
//
//        public PasswordWaiter(Context context, RequestPasswordListener listener) {
//            super();
//            mContext = context;
//            mDialog = new ProgressDialog(context);
//            mListener = listener;
//        }
//
//        protected void onPreExecute() {
//            mDialog.setMessage(mContext.getResources().getString(
//                    R.string.checking_password));
//            mDialog.setCancelable(false);
//            mDialog.show();
//        }
//
//        protected String doInBackground(String... params) {
//            try {
//                if (BoteHelper.tryPassword(params[0]))
//                    return null;
//                else {
//                    cancel(false);
//                    return mContext.getResources().getString(
//                            R.string.password_incorrect);
//                }
//            } catch (IOException e) {
//                cancel(false);
//                return mContext.getResources().getString(
//                        R.string.password_file_error);
//            } catch (GeneralSecurityException e) {
//                cancel(false);
//                return mContext.getResources().getString(
//                        R.string.password_file_error);
//            }
//        }
//
//        protected void onCancelled(String result) {
//            mDialog.dismiss();
//            requestPassword(mContext, mListener, result);
//        }
//
//        protected void onPostExecute(String result) {
//            // Password is valid
//            mDialog.dismiss();
//            if (mListener != null)
//                mListener.onPasswordVerified();
//        }
//    }

    public static String joinAddressNames(Collection<Address> s) throws PasswordException, GeneralSecurityException, IOException {
        StringBuilder builder = new StringBuilder();
        Iterator<Address> iter = s.iterator();
        while (iter.hasNext()) {
            String name = getName(iter.next().toString());
            builder.append(name);
            if (!iter.hasNext()) {
                break;
            }
            builder.append(", ");
        }
        return builder.toString();
    }

//    /**
//     * Attempt to revoke any URI permissions that were granted on an Email's attachments.
//     * This is best-effort; exceptions are silently ignored.
//     *
//     * @param context    the Context in which permissions were granted
//     * @param folderName where the Email is
//     * @param email      the Email to revoke permissions for
//     */
//    public static void revokeAttachmentUriPermissions(String folderName, Email email) {
//        List<Part> parts;
//        try {
//            parts = email.getParts();
//        } catch (Exception e) {
//            // Nothing we can do, abort
//            return;
//        }
//
//        for (Part part : parts) {
//            try {
//                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                    Uri uri = AttachmentProvider.getUriForAttachment(folderName,
//                            email.getMessageID(), parts.indexOf(part));
//                    context.revokeUriPermission(uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//            } catch (MessagingException e) {
//                // Ignore and carry on
//            }
//        }
//    }

    public static void copyStream(InputStream in, OutputStream out) {
        byte[] buf = new byte[8192];
        int len;

        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
//            Log.e(Constants.ANDROID_LOG_TAG, "Exception copying streams", e);
            System.out.println(BoteHelper.class.getSimpleName()+": IOException caught in copyStream():");
            e.printStackTrace();
        }
    }

//    public static String getHumanReadableSize(Context context, long size) {
//        int unit = (63 - Long.numberOfLeadingZeros(size)) / 10;   // 0 if totalBytes<1K, 1 if 1K<=totalBytes<1M, etc.
//        double value = (double) size / (1 << (10 * unit));
//        int formatStr;
//        switch (unit) {
//            case 0:
//                formatStr = R.string.n_bytes;
//                break;
//            case 1:
//                formatStr = R.string.n_kilobytes;
//                break;
//            default:
//                formatStr = R.string.n_megabytes;
//        }
//        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
//        if (value < 100)
//            formatter.setMaximumFractionDigits(1);
//        else
//            formatter.setMaximumFractionDigits(0);
//        return context.getString(formatStr, formatter.format(value));
//    }
}
