package io.onemfive.core.util.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TasksTest {

    private TaskRunner taskRunner;

    private class PrintTask extends BaseTask {

        private int id;

        public PrintTask(TaskRunner taskRunner, int id) {
            super("PrintTask", taskRunner);
            this.id = id;
        }

        @Override
        public Boolean execute() {
            System.out.println(getTaskName()+ id);
            return true;
        }
    }

    @Before
    public void init() {
        taskRunner = new TaskRunner();
        taskRunner.setRunUntil(System.currentTimeMillis() + (10 * 1000));
    }

    @Test
    public void taskExecutor() {
        PrintTask one = new PrintTask(taskRunner, 1);
        taskRunner.addTask(one);
        one.setDelayed(true);
        one.setDelayTimeMS(3 * 1000L);
        PrintTask two = new PrintTask(taskRunner, 2);
        taskRunner.addTask(two);
        taskRunner.run();
    }

    @After
    public void teardown() {
        taskRunner.shutdown();
    }

}
