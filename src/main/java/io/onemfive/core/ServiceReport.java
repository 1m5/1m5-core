package io.onemfive.core;

import io.onemfive.data.JSONSerializable;

import java.util.Map;

public class ServiceReport implements JSONSerializable {

    public String serviceClassName;
    public ServiceStatus serviceStatus;
    public Boolean registered = false;
    public Boolean running = false;

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public void fromMap(Map<String, Object> map) {

    }
}
