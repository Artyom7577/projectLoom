package structuredConcurrency;

import structuredConcurrency.LongRunningTask.TaskResponse;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LongRunningTask implements Callable<TaskResponse> {

    private final String name;
    private final int time;
    private final String output;
    private final boolean fail;

    public LongRunningTask(String name, int time, String output, boolean fail) {
        super();
        this.name = name;
        this.time = time;
        this.output = output;
        this.fail = fail;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("> Main : Started");

        LongRunningTask longRunningTask = new LongRunningTask("LongTask", 10, "json-response1", false);

        try (var service = Executors.newFixedThreadPool(2)) {

            Future<TaskResponse> taskFeature = service.submit(longRunningTask);

            Thread.sleep(Duration.ofSeconds(5));
            taskFeature.cancel(true);
        }

        System.out.println("> Main : Completed");

    }

    @Override
    public TaskResponse call() throws Exception {

        long start = System.currentTimeMillis();

        print("Started");
        int numSecs = 0;
        while (numSecs++ < time) {

            if(Thread.interrupted()) {
                throwExceptionOnFailure();
            }
            print("Working .." + numSecs);

            try {
                Thread.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException e) {
                throwExceptionOnFailure();
            }
        }

        if (fail) {
            throwExceptionOnFailure();
        }
        print("Completed");

        long end = System.currentTimeMillis();


        return new TaskResponse(this.name, this.output, (end - start));
    }

    private void throwExceptionOnFailure() {
        print("Failed");
        throw new RuntimeException(name + " : Failed");
    }

    private void print(String message) {
        System.out.printf("> %s : %s\n", name, message);
    }

    record TaskResponse(String name, String response, long timeTaken) {

    }
}
