package io.onemfive.core;

import io.onemfive.data.JSONSerializable;

import java.util.HashMap;
import java.util.Map;

public abstract class ServiceRequest implements JSONSerializable {
    public String errorMessage;
    public Exception exception;

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("errorMessage",errorMessage);
        m.put("exception",exception.getLocalizedMessage());
        return m;
    }

    public void fromMap(Map<String,Object> m) {
        if(m.get("errorMessage")!=null) this.errorMessage = (String)m.get("errorMessage");
        if(m.get("exception")!=null) {
            exception = new Exception((String)m.get("exception"));
        }
    }
}
