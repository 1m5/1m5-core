package io.onemfive.core.sensors;

import io.onemfive.core.util.AppThread;
import io.onemfive.core.util.Wait;
import io.onemfive.data.Envelope;
import io.onemfive.data.Peer;
import io.onemfive.data.Route;

import java.util.*;
import java.util.logging.Logger;

/**
 * Simple in-memory sensor management.
 */
public class SensorManagerSimple extends SensorManagerBase {

    private Logger LOG = Logger.getLogger(SensorManagerSimple.class.getName());

    @Override
    public Sensor selectSensor(Envelope e) {
        Sensor sensor = null;
        Route r = e.getRoute();
        // Lookup by Sensitivity
        if(e.getSensitivity() != null)
            sensor = lookupBySensitivity(e.getSensitivity());
        // Lookup by Operation
        if(sensor == null) {
            sensor = lookupByOperation(r.getOperation());
        }
        // Lookup by URL
        if(sensor == null && e.getURL() != null && e.getURL().getProtocol() != null){
            sensor = lookupByURL(e.getURL().getPath());
        }
        return sensor;
    }

    private Sensor lookupBySensitivity(Envelope.Sensitivity sensitivity) {
        int highestPriority = 0;
        Sensor highest = null;
        Collection<Sensor> sensors = activeSensors.values();
        for(Sensor s : sensors) {
            if(s.getSensitivity() == sensitivity && s.getPriority() >= highestPriority) {
                highest = s;
            }
        }
        return highest;
    }

    private Sensor lookupByOperation(String operation) {
        int highestPriority = 0;
        Sensor highest = null;
        Collection<Sensor> sensors = activeSensors.values();
        String[] ops;
        for(Sensor s : sensors) {
            ops = s.getOperationEndsWith();
            for(String op : ops) {
                if(op.equals(operation) && s.getPriority() >= highestPriority)
                    highest = s;
            }
        }
        return highest;
    }

    private Sensor lookupByURL(String url) {
        int highestPriority = 0;
        Sensor highest = null;
        Collection<Sensor> sensors = activeSensors.values();
        String[] urls;
        for(Sensor s : sensors) {
            urls = s.getURLBeginsWith();
            for(String u : urls) {
                if(u.equals(url) && s.getPriority() >= highestPriority)
                    highest = s;
            }
        }
        if(highest == null) {
            for(Sensor s : sensors) {
                urls = s.getURLEndsWith();
                for(String u : urls) {
                    if(u.equals(url) && s.getPriority() >= highestPriority)
                        highest = s;
                }
            }
        }
        return highest;
    }

    @Override
    public void updatePeer(Peer peer) {
        peers.put(peer.getAddress(), peer);
    }

    @Override
    public void sensorError(final String sensorID) {
        // Sensor has Error, restart it if number of restarts is not greater than 3
        if(activeSensors.get(sensorID) != null) {
            if(activeSensors.get(sensorID).getRestartAttempts() <= 3) {
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        activeSensors.get(sensorID).restart();
                    }
                }).start();
            } else {
                // Sensor is apparently not working. Unregister it.
                activeSensors.remove(sensorID);
            }
        }
    }

    @Override
    public boolean init(final Properties properties) {
        // TODO: Add loop with checks
        Collection<Sensor> sensors = registeredSensors.values();
        for(final Sensor s : sensors) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    s.start(properties);
                    activeSensors.put(s.getClass().getName(),s);
                }
            }).start();
        }
        return true;
    }

    @Override
    public boolean shutdown() {
        // TODO: Add loop with checks
        Collection<Sensor> sensors = activeSensors.values();
        for(final Sensor s : sensors) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    s.shutdown();
                    activeSensors.remove(s.getClass().getName());
                }
            }).start();
        }
        return true;
    }


}
