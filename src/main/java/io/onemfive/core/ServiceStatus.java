package io.onemfive.core;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public enum ServiceStatus {
    // Service Starting Up
    NOT_INITIALIZED, // Initial state
    INITIALIZING, // Initializing service configuration
    WAITING, // Waiting on a dependent Service status to go to RUNNING
    STARTING, // Starting Service
    RUNNING, // Service is running normally
    PARTIALLY_RUNNING, // Service is running normally although not everything is running but it's expected to be normal
    DEGRADED_RUNNING, // Service is running but in a degraded manner; likely no need for action, will hopefully come back to Running
    UNSTABLE, // Service is running but there could be issues; likely need to restart
    // Service Pausing (Not Yet Supported In Any Service)
    PAUSING, // Service will begin queueing all new requests while in-process requests will be completed
    PAUSED, // Service is queueing new requests and pre-pausing requests have completed
    UNPAUSING, // Service has stopped queueing new requests and is starting to resume normal operations
    // Service Shutdown
    SHUTTING_DOWN, // Service shutdown imminent - not clean, process likely getting killed - perform the minimum ASAP
    GRACEFULLY_SHUTTING_DOWN, // Ideal clean shutdown
    SHUTDOWN, // Was shutdown forcefully - expect potential file / state corruption
    GRACEFULLY_SHUTDOWN, // Shutdown was graceful - safe to assume no file / state corruption
    // Restarting
    RESTARTING, // Short for GRACEFULLY_SHUTTING_DOWN followed by INITIALIZING on up
    // Service Error
    ERROR // Likely need of Service restart
}
