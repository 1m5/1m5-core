package io.onemfive.core.util.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Runs Tasks based on Timers.
 *
 * @author objectorange
 */
public class TaskRunner implements Runnable {

    private static final Logger LOG = Logger.getLogger(TaskRunner.class.getName());

    public enum Status {Running, Stopping, Shutdown}

    private ThreadPoolExecutor fixedExecutor;
    private ScheduledThreadPoolExecutor scheduledExecutor;

    private long periodicity = 1000; // every two seconds check to see if a task needs running
    private List<Task> tasks = new ArrayList<>();
    private Status status = Status.Shutdown;
    private long runUntil = 0L;

    public TaskRunner() {
        // Default to two new thread pools with 4 threads each
        fixedExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
    }

    public TaskRunner(int fixedExecutorThreads, int scheduledExecutorThreads) {
        if(fixedExecutorThreads > 0) {
            fixedExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(fixedExecutorThreads);
        }
        if(scheduledExecutorThreads > 0) {
            scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(scheduledExecutorThreads);
        }
    }

    public TaskRunner(ThreadPoolExecutor fixedExecutor, ScheduledThreadPoolExecutor scheduledExecutor) {
        this.fixedExecutor = fixedExecutor;
        this.scheduledExecutor = scheduledExecutor;
    }

    public void setPeriodicity(Long periodicity) {
        this.periodicity = periodicity;
    }

    public Long getPeriodicity() {
        return periodicity;
    }

    public Status getStatus() {
        return status;
    }

    public void setRunUntil(Long runUntil) {
        this.runUntil = runUntil;
    }

    public void addTask(final Task t) {
        tasks.add(t);
    }

    public void removeTask(Task t, boolean forceStop) {
        if(t.getStatus() == Task.Status.Running) {
            LOG.info("Task asked to remove yet still running...");
            if(forceStop) {
                LOG.info("Attempting to force stop task...");
                t.forceStop();
            } else {
                LOG.info("Attempting to stop task...");
                t.stop();
            }
        }
        LOG.info("Removing task...");
        tasks.remove(t);
    }

    @Override
    public void run() {
        status = Status.Running;
        LOG.info("Task Runner running...");
        while(status == Status.Running) {
            try {
                LOG.info("Sleeping for "+(periodicity/1000)+" seconds..");
                synchronized (this) {
                    this.wait(periodicity);
                }
            } catch (InterruptedException ex) {
            }
            LOG.info("Awoke, begin running tasks...");
            for (final Task t : tasks) {
                if(t.getPeriodicity() == -1) {
                    LOG.info("Flagged to not run, skp...");
                    continue; // Flag to not run
                }
                if(t.getStatus() == Task.Status.Completed) {
                    LOG.info("Completed, remove and skip...");
                    removeTask(t, false);
                    continue;
                }
                if(t.getScheduled()) {
                    LOG.info("Scheduled, skip...");
                    continue;
                }
                if(t.getDelayed()) {
                    if (scheduledExecutor == null) {
                        scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
                    }
                    if (t.getPeriodicity() > 0) {
                        if (t.getPeriodicity() < periodicity) {
                            // Ensure time between runs is at least the lowest task periodicity
                            periodicity = t.getPeriodicity();
                        }
                        if (t.getFixedDelay()) {
                            scheduledExecutor.scheduleWithFixedDelay(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        } else {
                            scheduledExecutor.scheduleAtFixedRate(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        }
                    } else {
                        scheduledExecutor.schedule(t, t.getDelayTimeMS(), TimeUnit.MILLISECONDS);
                    }
                    t.setScheduled(true);
                } else if(t.getPeriodicity() > 0) {
                    if (t.getPeriodicity() < periodicity) {
                        // Ensure time between runs is at least the lowest task periodicity
                        periodicity = t.getPeriodicity();
                    }
                    if (t.getFixedDelay()) {
                        scheduledExecutor.scheduleWithFixedDelay(t, 0, t.getPeriodicity(), TimeUnit.MILLISECONDS);
                    } else {
                        scheduledExecutor.scheduleAtFixedRate(t, 0, t.getPeriodicity(), TimeUnit.MILLISECONDS);
                    }
                    t.setScheduled(true);
                } else if(!t.getScheduled()) {
                    if(t.getLongRunng()) {
                        fixedExecutor.execute(t);
                    } else {
                        t.execute();
                    }
                }
            }
            LOG.info("Tasks ran.");
            if(runUntil > 0 && runUntil < System.currentTimeMillis()) {
                status = Status.Stopping;
            }
        }
        LOG.info("Task Runner Stopped.");
        status = Status.Shutdown;
    }

    public void shutdown() {
        LOG.info("Shutting down Task Runner...");
        status = Status.Stopping;
        fixedExecutor.shutdown();
        scheduledExecutor.shutdown();
        status = Status.Shutdown;
        LOG.info("Task Runner shutdown.");
    }

}
