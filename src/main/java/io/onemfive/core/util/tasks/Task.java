package io.onemfive.core.util.tasks;

import java.util.Map;

public interface Task extends Runnable {

    enum Status {Ready, Running, Completed}

    String getTaskName();
    void setParams(Map<Object,Object> params);
    void addParams(Map<Object,Object> params);
    Long getPeriodicity();
    void setLastCompletionTime(Long lastCompletionTime);
    Long getLastCompletionTime();
    void setDelayed(Boolean delayed);
    Boolean getDelayed();
    void setDelayTimeMS(Long delayTimeMS);
    void setFixedDelay(Boolean fixedDelay);
    Boolean getFixedDelay();
    Long getDelayTimeMS();
    void setLongRunning(Boolean longRunning);
    Boolean getLongRunng();
    void setScheduled(Boolean scheduled);
    Boolean getScheduled();
    Boolean getSuccessful();
    Boolean execute();
    Boolean stop();
    Boolean forceStop();
    Status getStatus();
}
