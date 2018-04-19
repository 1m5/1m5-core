package io.onemfive.core.prana;

import java.io.Serializable;

/**
 * Provides Amino Service Information for this specific node.
 *
 * @author objectorange
 */
public class PranaConfiguration implements Serializable {

    public static final String NAME = "io.synapticcelerity.amino.PranaConfiguration";

    /**
     * Is CPU currently shareable
     */
    private boolean cpuShareable = false;
    /**
     * Share the CPU
     */
    private boolean shareCPU = false;
    /**
     * Max percent of CPU that can be shared
     */
    private int maxCPUShareable = 0; // In percent
    /**
     * Amino Price per GHz/Second
     */
    private double aminoPerGHzPerSecond = 0;
    /**
     * Max GHz/Second of Device
     */
    private double deviceMaxGHzPerSecond = 1.0;
    /**
     * Number of CPU Cores of Device
     */
    private int deviceCPUCores = 1;

    /**
     * Is Network Bandwidth Shareable
     */
    private boolean networkShareable = false;
    /**
     * Share the Network
     */
    private boolean shareNetwork = false;
    /**
     * Max Mbps to share
     */
    private long maxNetworkShareableMbps = 0;
    /**
     * Amino Price per Mb
     */
    private double aminoPerNetworkMb = 0.0;
    /**
     *
     */
    private int maxNetworkBandwidthMbps = 5;

    /**
     * Is Storage Shared
     */
    private boolean storageShared = false;
    /**
     * Share Storage. This indicates if storage requests are accepted.
     * It's possible that storageShared is true and this
     * variable is false if storage was shared at one
     * point but is no longer accepting new storage requests.
     */
    private boolean shareStorage = false;
    /**
     * Max MBs Storage Shareable
     */
    private long maxStorageShareableMBs = 0;
    /**
     * Current MBs Storage Shareable
     */
    private long currentStoreShareableMBs = 0;
    /**
     * Amino Price per MB
     */
    private double aminoPerStorageMB = 0;
    /**
     * Max Storage of Device in MBs
     */
    private long maxStorageDeviceMBs = 4000; // Default to 4 GBs

    private boolean storageLevelAcceptable = false;
    private boolean batteryLevelAcceptable = false;
    private boolean powerConnected = false;

    public boolean getCpuShareable() {
        return cpuShareable;
    }

    public void setCpuShareable(boolean cpuShareable) {
        this.cpuShareable = cpuShareable;
    }

    public boolean getShareCPU() {
        return shareCPU;
    }

    public void setShareCPU(boolean shareCPU) {
        this.shareCPU = shareCPU;
    }

    public int getMaxCPUShareable() {
        return maxCPUShareable;
    }

    public void setMaxCPUShareable(int maxCPUShareable) {
        this.maxCPUShareable = maxCPUShareable;
    }

    public double getAminoPerGHzPerSecond() {
        return aminoPerGHzPerSecond;
    }

    public void setAminoPerGHzPerSecond(double aminoPerGHzPerSecond) {
        this.aminoPerGHzPerSecond = aminoPerGHzPerSecond;
    }

    public double getDeviceMaxGHzPerSecond() {
        return deviceMaxGHzPerSecond;
    }

    public void setDeviceMaxGHzPerSecond(double deviceMaxGHzPerSecond) {
        this.deviceMaxGHzPerSecond = deviceMaxGHzPerSecond;
    }

    public int getDeviceCPUCores() {
        return deviceCPUCores;
    }

    public void setDeviceCPUCores(int deviceCPUCores) {
        this.deviceCPUCores = deviceCPUCores;
    }

    public boolean getNetworkShareable() {
        return networkShareable;
    }

    public void setNetworkShareable(boolean networkShareable) {
        this.networkShareable = networkShareable;
    }

    public boolean getShareNetwork() {
        return shareNetwork;
    }

    public void setShareNetwork(boolean shareNetwork) {
        this.shareNetwork = shareNetwork;
    }

    public long getMaxNetworkShareableMbps() {
        return maxNetworkShareableMbps;
    }

    public void setMaxNetworkShareableMbps(long maxNetworkShareableMbps) {
        this.maxNetworkShareableMbps = maxNetworkShareableMbps;
    }

    public double getAminoPerNetworkMb() {
        return aminoPerNetworkMb;
    }

    public void setAminoPerNetworkMb(double aminoPerNetworkMb) {
        this.aminoPerNetworkMb = aminoPerNetworkMb;
    }

    public int getMaxNetworkBandwidthMbps() {
        return maxNetworkBandwidthMbps;
    }

    public void setMaxNetworkBandwidthMbps(int maxNetworkBandwidthMbps) {
        this.maxNetworkBandwidthMbps = maxNetworkBandwidthMbps;
    }

    public boolean getStorageShared() {
        return storageShared;
    }

    public void setStorageShared(boolean storageShared) {
        this.storageShared = storageShared;
    }

    public boolean getShareStorage() {
        return shareStorage;
    }

    public void setShareStorage(boolean shareStorage) {
        this.shareStorage = shareStorage;
    }

    public long getMaxStorageShareableMBs() {
        return maxStorageShareableMBs;
    }

    public void setMaxStorageShareableMBs(long maxStorageShareableMBs) {
        this.maxStorageShareableMBs = maxStorageShareableMBs;
    }

    public long getCurrentStoreShareableMBs() {
        return currentStoreShareableMBs;
    }

    public void setCurrentStoreShareableMBs(long currentStoreShareableMBs) {
        this.currentStoreShareableMBs = currentStoreShareableMBs;
    }

    public double getAminoPerStorageMB() {
        return aminoPerStorageMB;
    }

    public void setAminoPerStorageMB(double aminoPerStorageMB) {
        this.aminoPerStorageMB = aminoPerStorageMB;
    }

    public long getMaxStorageDeviceMBs() {
        return maxStorageDeviceMBs;
    }

    public void setMaxStorageDeviceMBs(long maxStorageDeviceMBs) {
        this.maxStorageDeviceMBs = maxStorageDeviceMBs;
    }

    public boolean getStorageLevelAcceptable() {
        return storageLevelAcceptable;
    }

    public void setStorageLevelAcceptable(boolean storageLevelAcceptable) {
        this.storageLevelAcceptable = storageLevelAcceptable;
    }

    public boolean getBatteryLevelAcceptable() {
        return batteryLevelAcceptable;
    }

    public void setBatteryLevelAcceptable(boolean batteryLevelAcceptable) {
        this.batteryLevelAcceptable = batteryLevelAcceptable;
    }

    public boolean getPowerConnected() {
        return powerConnected;
    }

    public void setPowerConnected(boolean powerConnected) {
        this.powerConnected = powerConnected;
    }
}
