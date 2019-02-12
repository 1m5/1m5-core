package io.onemfive.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Enforcing a nonce long.
 *
 * @author objectorange
 */
public class Nonce {

    private static Logger LOG = Logger.getLogger(Nonce.class.getName());

    private List<Long> nonceList = new ArrayList<>(); // Envelope id
    private int maxNonceListSize; // to prevent the nonce list from consuming excess memory
    private int prunePercentSize; // size in percent to prune once maxNonceListSize reached

    public Nonce() {
        this(new ArrayList<Long>(), 1000000, 10); // 1 million (8Mb)
    }

    public Nonce(int maxNonceListSize) {
        this(new ArrayList<Long>(), maxNonceListSize, 10);
    }

    /**
     * Supports setting nonceList from prior persistence.
     * @param nonceList
     * @param maxNonceListSize
     */
    public Nonce(List<Long> nonceList, int maxNonceListSize, int prunePercentSize) {
        this.nonceList  = nonceList;
        this.maxNonceListSize = maxNonceListSize;
        if(prunePercentSize < 0) prunePercentSize = 0;
        else if(prunePercentSize > 100) prunePercentSize = 100;
        this.prunePercentSize = prunePercentSize;
    }

    public boolean continueOn(long id) {
        pruneNonceList();
        if(nonceList.contains(id))
            return false;
        else
            nonceList.add(id);
        return true;
    }

    private void pruneNonceList() {
//        LOG.info(nonceList.size()+" ids in nonce list. Max size = "+maxNonceListSize);
        if(nonceList.size() > maxNonceListSize) {
            LOG.info("Pruning nonce list by "+prunePercentSize+"%...");
            if(prunePercentSize==100) {
                nonceList.clear();
                LOG.info("Nonce list cleared.");
            } else {
                int numberToPrune = maxNonceListSize * (prunePercentSize / 100);
                for (int i = 0; i < numberToPrune; i++) {
                    // Remove earliest
                    nonceList.remove(0);
                }
                LOG.info(numberToPrune + " pruned.");
            }

        }
    }

    public static void main(String[] args) {
        Nonce n = new Nonce(100);
        List<Long> l = new ArrayList<>();
        for(long i=1; i<300; i++) {
            n.continueOn(i);
        }
    }
}
