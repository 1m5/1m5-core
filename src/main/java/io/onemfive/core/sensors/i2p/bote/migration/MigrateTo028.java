package io.onemfive.core.sensors.i2p.bote.migration;

import io.onemfive.core.sensors.i2p.bote.Configuration;
import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.addressbook.AddressBook;
import io.onemfive.core.sensors.i2p.bote.email.EmailDestination;
import io.onemfive.core.sensors.i2p.bote.email.EmailIdentity;
import io.onemfive.core.sensors.i2p.bote.email.Identities;
import io.onemfive.core.sensors.i2p.bote.email.Identities.IdentityComparator;
import io.onemfive.core.sensors.i2p.bote.fileencryption.EncryptedInputStream;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordHolder;
import io.onemfive.core.sensors.i2p.bote.packet.dht.Contact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import net.i2p.util.Log;

/**
 * Migrates the identities file and the address book to the 0.2.8 format.<br/>
 * It cannot be called at startup like {@link MigrateTo026} and
 * {@link MigrateTo027} because it operates on password-protected data.
 */
class MigrateTo028 {
    private Log log = new Log(MigrateTo028.class);

    public void migrateIfNeeded(Configuration configuration, PasswordHolder passwordHolder) throws FileNotFoundException, IOException, GeneralSecurityException, PasswordException {
        migrateIdentitiesIfNeeded(configuration, passwordHolder);
        migrateAddressBookIfNeeded(configuration, passwordHolder);
    }

    private void migrateIdentitiesIfNeeded(Configuration configuration, PasswordHolder passwordHolder) throws FileNotFoundException, IOException, GeneralSecurityException, PasswordException {
        File identitiesFile = configuration.getIdentitiesFile();
        if (!identitiesFile.exists())
            return;

        BufferedReader input = null;
        try {
            InputStream encryptedStream = new EncryptedInputStream(new FileInputStream(identitiesFile), passwordHolder);
            input = new BufferedReader(new InputStreamReader(encryptedStream));

            // No PasswordException occurred, so read the input stream and call migrateIdentities() if needed
            List<String> lines = Util.readLines(encryptedStream);
            if (!isIdentitiesFileMigrated(lines))
                migrateIdentities(lines, configuration, passwordHolder);
        } finally {
            if (input != null)
                input.close();
        }
    }

    /**
     * Returns <code>true</code> if the identities file is in the 0.2.8 format.
     * @param lines Contents of the identities file
     */
    private boolean isIdentitiesFileMigrated(List<String> lines) {
        if (lines.isEmpty())
            return true;
        String firstLine = lines.get(0);
        if (firstLine.startsWith("#")) {
            if (lines.size() > 1)
                firstLine = lines.get(1);
            else
                return true;
        }
        return firstLine.startsWith("Default=") ||
                firstLine.startsWith("default=") ||
                firstLine.startsWith("identity0.");
    }

    /**
     * @param configuration
     * @throws PasswordException
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws Exception
     */
    private void migrateIdentities(List<String> lines, Configuration configuration, PasswordHolder passwordHolder) throws IOException, GeneralSecurityException, PasswordException {
        SortedSet<EmailIdentity> identitiesSet = new TreeSet<EmailIdentity>(new IdentityComparator());
        String defaultIdentityString = null;
        for (String line: lines) {
            if (line.toLowerCase().startsWith("default"))
                defaultIdentityString = line.substring("default ".length());
            else {
                EmailIdentity identity = parse(line);
                if (identity != null)
                    identitiesSet.add(identity);
            }
        }

        // set the default identity; if none defined, make the first one the default
        EmailIdentity defaultIdentity = get(identitiesSet, defaultIdentityString);
        if (defaultIdentity != null)
            defaultIdentity.setDefaultIdentity(true);
        else if (!identitiesSet.isEmpty())
            identitiesSet.iterator().next().setDefaultIdentity(true);

        Identities identities = new Identities(configuration.getIdentitiesFile(), passwordHolder);
        for (EmailIdentity identity: identitiesSet)
            identities.add(identity);
        identities.save();
    }

    private EmailIdentity get(Collection<EmailIdentity> identities, String key) {
        for (EmailIdentity identity: identities)
            if (identity.getKey().equals(key))
                return identity;
        return null;
    }

    private EmailIdentity parse(String emailIdentityString) {
        String[] fields = emailIdentityString.split("\\t", 4);
        if (fields.length < 2) {
            log.error("Unparseable email identity: <" + emailIdentityString + ">");
            return null;
        }
        try {
            EmailIdentity identity = new EmailIdentity(fields[0]);
            if (fields.length > 1)
                identity.setPublicName(fields[1]);
            if (fields.length > 2)
                identity.setDescription(fields[2]);
            if (fields.length > 3)
                identity.setEmailAddress(fields[3]);
            return identity;
        }
        catch (PatternSyntaxException e) {
            log.error("Unparseable email identity: <" + emailIdentityString + ">", e);
            return null;
        } catch (GeneralSecurityException e) {
            log.error("Invalid email identity: <" + fields[0] + ">", e);
            return null;
        }
    }

    private boolean isAddressBookFileMigrated(List<String> lines) {
        if (lines.isEmpty())
            return true;
        String firstLine = lines.get(0);
        if (firstLine.startsWith("#")) {
            if (lines.size() > 1)
                firstLine = lines.get(1);
            else
                return true;
        }
        return firstLine.startsWith("contact0.");
    }

    private void migrateAddressBookIfNeeded(Configuration configuration, PasswordHolder passwordHolder) throws FileNotFoundException, IOException, GeneralSecurityException, PasswordException {
        File addressBookFile = configuration.getAddressBookFile();
        if (!addressBookFile.exists())
            return;

        BufferedReader input = null;
        try {
            InputStream encryptedStream = new EncryptedInputStream(new FileInputStream(addressBookFile), passwordHolder);
            input = new BufferedReader(new InputStreamReader(encryptedStream));

            // No PasswordException occurred, so read the input stream and call migrateAddressBook() if needed
            List<String> lines = Util.readLines(encryptedStream);
            if (!isAddressBookFileMigrated(lines))
                migrateAddressBook(lines, configuration, passwordHolder);
        } finally {
            if (input != null)
                input.close();
        }
    }

    private void migrateAddressBook(List<String> lines, Configuration configuration, PasswordHolder passwordHolder) throws IOException, GeneralSecurityException, PasswordException {
        AddressBook addressBook = new AddressBook(configuration.getAddressBookFile(), passwordHolder);
        for (String line: lines) {
            String[] fields = line.split("\\t", 2);
            try {
                EmailDestination destination = new EmailDestination(fields[0]);
                String name = null;
                if (fields.length > 1)
                    name = fields[1];
                Contact contact = new Contact(name, destination);
                addressBook.add(contact);
            }
            catch (GeneralSecurityException e) {
                log.error("Not a valid Email Destination: <" + fields[0] + ">", e);
            }
        }
        addressBook.save();
    }
}
