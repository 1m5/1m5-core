package io.onemfive.core.sensors.i2p.bote.debug;

import io.onemfive.core.sensors.i2p.bote.Configuration;
import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.fileencryption.EncryptedInputStream;
import io.onemfive.core.sensors.i2p.bote.fileencryption.FileEncryptionUtil;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordException;
import io.onemfive.core.sensors.i2p.bote.fileencryption.PasswordHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.i2p.util.Log;

public class DebugSupport {
    private Log log = new Log(DebugSupport.class);
    private Configuration configuration;
    private PasswordHolder passwordHolder;

    public DebugSupport(Configuration configuration, PasswordHolder passwordHolder) {
        this.configuration = configuration;
        this.passwordHolder = passwordHolder;
    }

    /**
     * Tests all encrypted I2P-Bote files and returns a list containing those that
     * cannot be decrypted.
     * @return A list of problem files, or an empty list if no problems were found
     * @throws PasswordException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public List<File> getUndecryptableFiles() throws PasswordException, IOException, GeneralSecurityException {
        // make sure the password is correct
        byte[] password = passwordHolder.getPassword();
        if (password == null)
            throw new PasswordException();
        File passwordFile = configuration.getPasswordFile();
        boolean correct = FileEncryptionUtil.isPasswordCorrect(password, passwordFile);
        if (!correct)
            throw new PasswordException();

        // make a list of all encrypted files
        List<File> files = new ArrayList<File>();
        files.add(configuration.getIdentitiesFile());
        files.add(configuration.getAddressBookFile());
        File[] emailFolders = new File[] {configuration.getInboxDir(), configuration.getOutboxDir(), configuration.getSentFolderDir(), configuration.getTrashFolderDir()};;
        for (File dir: emailFolders)
            files.addAll(Arrays.asList(dir.listFiles()));

        for (Iterator<File> iter=files.iterator(); iter.hasNext(); ) {
            File file = iter.next();
            FileInputStream stream = new FileInputStream(file);
            try {
                Util.readBytes(new EncryptedInputStream(stream, password));
                // no PasswordException or other exception occurred, so the file is good
                iter.remove();
            } catch (Exception e) {
                // leave the file in the list and log
                log.debug("Can't decrypt file <" + file.getAbsolutePath() + ">", e);
            } finally {
                if (stream != null)
                    stream.close();
            }
        }

        return files;
    }
}
