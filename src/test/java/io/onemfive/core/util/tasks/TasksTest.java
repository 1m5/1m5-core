package io.onemfive.core.util.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TasksTest {

    private TaskRunner taskRunner;
    private int i = 1;

    private class PrintTask extends BaseTask {

        public PrintTask(TaskRunner taskRunner) {
            super("PrintTask", taskRunner);
        }

        @Override
        public Boolean execute() {
            System.out.println(getTaskName()+ i++);
            return true;
        }
    }

    @Before
    public void init() {
        taskRunner = new TaskRunner();
    }

    @Test
    public void taskExecutor() {
        taskRunner.addTask(new PrintTask(taskRunner));
        taskRunner.run();
    }

    @After
    public void teardown() {
        taskRunner.shutdown();
    }

}
