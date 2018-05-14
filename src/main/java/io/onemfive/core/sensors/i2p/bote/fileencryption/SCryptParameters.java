package io.onemfive.core.sensors.i2p.bote.fileencryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Contains parameters specific to the <code>scrypt</code> key derivation function.
 * @see FileEncryptionUtil
 */
public class SCryptParameters {
    public final int N;
    public final int r;
    public final int p;

    /**
     * @param N CPU cost parameter
     * @param r Memory cost parameter
     * @param p Parallelization parameter
     */
    public SCryptParameters(int N, int r, int p) {
        this.N = N;
        this.r = r;
        this.p = p;
    }

    public SCryptParameters(InputStream input) throws IOException {
        this(new DataInputStream(input));
    }

    public SCryptParameters(DataInputStream input) throws IOException {
        N = input.readInt();
        r = input.readInt();
        p = input.readInt();
    }

    public void writeTo(OutputStream output) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(output);
        dataStream.writeInt(N);
        dataStream.writeInt(r);
        dataStream.writeInt(p);
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject == null)
            return false;
        if (!(anotherObject.getClass() == getClass()))
            return false;
        SCryptParameters otherParams = (SCryptParameters)anotherObject;

        return otherParams.N==N && otherParams.r==r && otherParams.p==p;
    }

    /** Overridden because <code>equals</code> is overridden */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + N;
        result = prime * result + p;
        result = prime * result + r;
        return result;
    }
}
