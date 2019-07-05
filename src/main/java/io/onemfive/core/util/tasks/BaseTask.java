package io.onemfive.core.util.tasks;

import java.util.Map;
import java.util.logging.Logger;

public abstract class BaseTask implements Task {

    private static final Logger LOG = Logger.getLogger(BaseTask.class.getName());

    protected final String taskName;
    protected TaskRunner taskRunner;
    protected Map<Object,Object> params;
    protected long periodicity = 0L; // 0 = Single task (default); > 0 is periodic execution
    protected long lastCompletionTime = 0L;
    protected boolean delayed = false;
    protected Long delayTimeMS = 0L;
    protected boolean fixedDelay = false;
    protected long startTime = 0L;
    protected long completionTime = 0L;
    protected boolean longRunning = false;
    protected boolean successful = false;
    protected boolean stopASAP = false;
    protected boolean scheduled = false;
    protected Task.Status status = Task.Status.Ready;

    public BaseTask(String taskName, TaskRunner taskRunner) {
        this.taskName = taskName;
        this.taskRunner = taskRunner;
    }

    @Override
    public void setParams(Map<Object, Object> params) {
        this.params = params;
    }

    @Override
    public void addParams(Map<Object, Object> params) {
        if(this.params == null) {
            this.params = params;
        } else {
            this.params.putAll(params);
        }
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public Long getPeriodicity() {
        return periodicity;
    }

    @Override
    public Long getLastCompletionTime() {
        return lastCompletionTime;
    }

    @Override
    public void setLastCompletionTime(Long lastCompletionTime) {
        this.lastCompletionTime = lastCompletionTime;
    }

    @Override
    public void setDelayed(Boolean delayed) {
        this.delayed = delayed;
    }

    @Override
    public Boolean getDelayed() {
        return this.delayed;
    }

    @Override
    public void setDelayTimeMS(Long delayTimeMS) {
        this.delayTimeMS = delayTimeMS;
    }

    @Override
    public Long getDelayTimeMS() {
        return delayTimeMS;
    }

    @Override
    public void setFixedDelay(Boolean fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    @Override
    public Boolean getFixedDelay() {
        return fixedDelay;
    }

    @Override
    public void setLongRunning(Boolean longRunning) {
        this.longRunning = longRunning;
    }

    @Override
    public Boolean getLongRunng() {
        return longRunning;
    }

    @Override
    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

    @Override
    public Boolean getScheduled() {
        return scheduled;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void run() {

        status = Status.Running;
        startTime = System.currentTimeMillis();
        successful = execute();
        completionTime = System.currentTimeMillis();
        lastCompletionTime = completionTime;
        if(periodicity <= 0) {
            status = Status.Completed;
        }
    }

    @Override
    public Boolean stop() {
        stopASAP = true;
        return true;
    }

    @Override
    public Boolean forceStop() {
        Thread.currentThread().interrupt();
        return true;
    }
}
