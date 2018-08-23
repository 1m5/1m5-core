package io.onemfive.core.util.fileencryption;

import io.onemfive.core.Util;
import net.i2p.I2PAppContext;
import net.i2p.data.Base64;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static io.onemfive.core.util.fileencryption.FileEncryptionConstants.SALT_LENGTH;


/**
 * A command line program for encrypting files in the I2P-Bote format.
 *
 * Originally from I2P-Bote.
 */
public class Encrypt {
    private static final String VERBOSE_OPTION = "-v";
    private static final String DERIV_PARAMS_FILE_OPTION = "-d";
    
    private boolean verbose;
    private File inputFile;
    private File outputFile;
    private File derivParamsFile;
    
    private Encrypt(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        init(args);
        
        if (!inputFile.exists()) {
            System.err.println("File not found: " + inputFile.getAbsolutePath());
            System.exit(1);
        }
        
        byte[] password = promptForPassword();
        if (password == null)
            System.exit(0);
        
        InputStream input = null;
        OutputStream encryptedOutputStream = null;
        try {
            DerivedKey derivedKey = getDerivedKey(password, inputFile);
            input = new FileInputStream(inputFile);
            
            OutputStream output;
            if (outputFile == null)
                output = System.out;
            else
                output = new FileOutputStream(outputFile);

            if (verbose) {
                System.out.println("Parameters:");
                System.out.println("  N = " + derivedKey.scryptParams.N);
                System.out.println("  r = " + derivedKey.scryptParams.r);
                System.out.println("  p = " + derivedKey.scryptParams.p);
                System.out.println("  Salt = " + Base64.encode(derivedKey.salt));
            }
            
            encryptedOutputStream = new EncryptedOutputStream(output, derivedKey);
            Util.copy(input, encryptedOutputStream);
            
            if (verbose)
                System.out.println("Encryption finished.");
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getLocalizedMessage());
            System.exit(1);
        } catch (GeneralSecurityException e) {
            System.err.println("Error: " + e.getLocalizedMessage());
            System.exit(1);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("Error closing input file.");
                }
            if (encryptedOutputStream != null)
                try {
                    encryptedOutputStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing output file.");
                }
        }
    }
    
    private void printUsage() {
        System.out.println("Syntax: Encrypt [v] [-d file] <input file> [output file]");
        System.out.println();
        System.out.println("Encrypts an input file and writes it to an output file.");
        System.out.println("Existing files are overwritten without warning.");
        System.out.println("If no output file is given, stdout is used instead.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  " + VERBOSE_OPTION + "        Print some additional messages");
        System.out.println("  " + DERIV_PARAMS_FILE_OPTION + " file   Read derivation parameters from this file");
    }
    
    /**
     * Reads command line parameters into instance variables
     */
    private void init(String[] args) {
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (VERBOSE_OPTION.equals(arg))
                verbose = true;
            else if (DERIV_PARAMS_FILE_OPTION.equals(arg)) {
                i++;
                if (i >= args.length)
                    System.err.println("Warning: " + DERIV_PARAMS_FILE_OPTION + " not followed by a filename, ignoring.");
                else
                    derivParamsFile = new File(args[i]);
            }
            else if (inputFile == null)
                inputFile = new File(arg);
            else
                outputFile = new File(arg);
        }
        
        if (verbose) {
            System.out.println("Input file: " + inputFile.getAbsolutePath());
            if (outputFile == null)
                System.out.println("No output file given, writing to stdout.");
            else
                System.out.println("Output file: " + outputFile.getAbsolutePath());
        }
    }
    
    private byte[] promptForPassword() {
        System.out.print("Enter a password: ");
        char[] passwordChars1 = System.console().readPassword();
        System.out.print("Enter the password again: ");
        char[] passwordChars2 = System.console().readPassword();
        if (!Arrays.equals(passwordChars1, passwordChars2)) {
            System.err.println("The two password don't match.");
            System.exit(1);
        }
        if (passwordChars1 != null)
            return new String(passwordChars1).getBytes();
        else
            return null;
    }
    
    private DerivedKey getDerivedKey(byte[] password, File inputFile) throws GeneralSecurityException {
        if (derivParamsFile == null) {
            // the derivation parameters file is in the I2P-Bote root dir, so search all parent directories
            File parentDir = inputFile.getAbsoluteFile().getParentFile();
            while (derivParamsFile==null && parentDir!=null) {
                boolean paramsFileFound = Util.contains(parentDir, "derivparams");
                if (paramsFileFound)
                    derivParamsFile = new File(parentDir, "derivparams");
                else
                    parentDir = parentDir.getParentFile();
            }
        }
        
        if (derivParamsFile != null) {
            if (verbose)
                System.out.println("Using derivation parameters file: " + derivParamsFile.getAbsolutePath());
            try {
                return FileEncryptionUtil.getEncryptionKey(password, derivParamsFile);
            }
            catch (IOException e) {
                System.out.println("Can't create key from derivation parameters file: " + e.getLocalizedMessage());
            }
        }
        
        // derivation parameters file not found or not readable, use default params and random salt
        if (verbose)
            System.out.println("No derivation parameters file available, creating new salt.");
        I2PAppContext appContext = I2PAppContext.getGlobalContext();
        byte[] salt = new byte[SALT_LENGTH];
        appContext.random().nextBytes(salt);
        byte[] key = FileEncryptionUtil.getEncryptionKey(password, salt, FileEncryptionConstants.KDF_PARAMETERS);
        return new DerivedKey(salt, FileEncryptionConstants.KDF_PARAMETERS, key);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new Encrypt(args);
    }
}