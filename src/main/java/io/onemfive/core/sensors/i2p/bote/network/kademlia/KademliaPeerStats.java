package io.onemfive.core.sensors.i2p.bote.network.kademlia;

import net.i2p.data.Destination;
import net.i2p.data.Hash;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.onemfive.core.sensors.i2p.bote.I2PBote;
import io.onemfive.core.sensors.i2p.bote.Util;
import io.onemfive.core.sensors.i2p.bote.network.DhtPeerStats;
import io.onemfive.core.sensors.i2p.bote.network.DhtPeerStatsRenderer;
import io.onemfive.core.sensors.i2p.bote.network.DhtPeerStatsRow;

/**
 * Stores information on Kademlia peers.
 * @see DhtPeerStats
 */
class KademliaPeerStats implements DhtPeerStats {
    private List<String> header;
    private List<DhtPeerStatsRow> data;

    KademliaPeerStats(DhtPeerStatsRenderer renderer, SBucket sBucket, List<KBucket> kBuckets,
                      Hash localDestinationHash) {
        String[] headerArray = new String[] {
                renderer.translateHeading(Columns.PEER),
                renderer.translateHeading(Columns.DESTINATION),
                renderer.translateHeading(Columns.BUCKET_PREFIX),
                renderer.translateHeading(Columns.DISTANCE),
                renderer.translateHeading(Columns.LOCKED),
                renderer.translateHeading(Columns.FIRST_SEEN),
        };
        header = Arrays.asList(headerArray);

        data = new ArrayList<DhtPeerStatsRow>();
        addPeerData(renderer, sBucket, localDestinationHash);
        for (KBucket kBucket: kBuckets)
            addPeerData(renderer, kBucket, localDestinationHash);
    }

    private void addPeerData(DhtPeerStatsRenderer renderer, AbstractBucket bucket,
                             Hash localDestinationHash) {
        for (KademliaPeer peer: bucket) {
            BigInteger distance = KademliaUtil.getDistance(localDestinationHash, peer.calculateHash());
            data.add(new KademliaPeerStatsRow(
                    renderer,
                    data.size() + 1,
                    peer,
                    getBucketPrefix(renderer, bucket),
                    distance,
                    peer.isLocked(),
                    peer.getConsecTimeouts(),
                    peer.getFirstSeen()
            ));
        }
    }

    /**
     * Returns the common prefix shared by the binary representation of all peers in the bucket.
     * @param bucket
     * @return
     */
    private String getBucketPrefix(DhtPeerStatsRenderer renderer, AbstractBucket bucket) {
        if (bucket instanceof KBucket) {
            KBucket kBucket = (KBucket)bucket;
            String prefix = kBucket.getBucketPrefix();
            if (prefix.isEmpty())
                return renderer.translateContent(Content.BUCKET_PREFIX_NONE);
            else
                return prefix;
        }
        else
            return renderer.translateContent(Content.BUCKET_PREFIX_S);
    }

    @Override
    public List<String> getHeader() {
        return header;
    }

    @Override
    public List<DhtPeerStatsRow> getData() {
        return data;
    }

    private static class KademliaPeerStatsRow implements DhtPeerStatsRow {
        private final DhtPeerStatsRenderer renderer;
        private final int peer;
        private final Destination destination;
        private final String bucketPrefix;
        private final BigInteger distance;
        private final boolean isLocked;
        private final int consecTimeouts;
        private final long firstSeen;

        KademliaPeerStatsRow(DhtPeerStatsRenderer renderer, int peer, Destination destination,
                             String bucketPrefix, BigInteger distance, boolean isLocked,
                             int consecTimeouts, long firstSeen) {
            this.renderer = renderer;
            this.peer = peer;
            this.destination = destination;
            this.bucketPrefix = bucketPrefix;
            this.distance = distance;
            this.isLocked = isLocked;
            this.consecTimeouts = consecTimeouts;
            this.firstSeen = firstSeen;
        }

        @Override
        public boolean isReachable() {
            return !isLocked;
        }

        @Override
        public List<String> toStrings() {
            Locale locale = new Locale(I2PBote.getLanguage());
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
            List<String> row = new ArrayList<String>();
            row.add(String.valueOf(peer));
            row.add(Util.toBase32(destination));
            row.add(bucketPrefix);
            row.add(distance.shiftRight((Hash.HASH_LENGTH-2)*8).toString());   // show the 2 most significant bytes
            row.add(String.valueOf(isLocked ?
                    renderer.translateContent(Content.YES)+"("+consecTimeouts+")" :
                    renderer.translateContent(Content.NO)));
            String firstSeen = formatter.format(this.firstSeen);
            row.add(String.valueOf(firstSeen));
            return row;
        }
    }
}
