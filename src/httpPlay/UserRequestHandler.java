package httpPlay;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class UserRequestHandler implements Callable<String> {

    @Override
    public String call() throws Exception {
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {

            String output = CompletableFuture
                    .supplyAsync(this::dbCall, service)
                    .thenCombine(CompletableFuture.supplyAsync(this::restCall, service)
                            , (result1, result2) -> {
                                return "[ " + result1 + "," + result2 + " ]";
                            })
                    .thenApply(result -> {
                        // both db call and restCall have completed
                        String r = externalCall();
                        return "[" + result + "," + r + "]";
                    })
                    .join();
            System.out.println(output);
            return output;
        }
    }

    private String concurrentCallFunction() throws InterruptedException {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            String collectedResult = executorService.invokeAll(Arrays.asList(this::dbCall, this::restCall))
                    .stream()
                    .map((f) -> {
                        try {
                            return (String) f.get();
                        } catch (ExecutionException | InterruptedException e) {
                            return null;
                        }
                    })
                    .collect(Collectors.joining(","));

            return "[ " + collectedResult + " ]";
        }
    }

    private String concurrentCallWithFutures() throws InterruptedException, ExecutionException {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            long start = System.currentTimeMillis();

            Future<String> dbFuture = executorService.submit(this::dbCall);
            Future<String> restFuture = executorService.submit(this::restCall);

            String result = String.format("[%s %s]", dbFuture.get(), restFuture.get());
            long end = System.currentTimeMillis();

            System.out.println("time = " + (end - start));

            System.out.println(result);
            return result;

        }
    }

    private String sequentialCall() throws Exception {
        long start = System.currentTimeMillis();
        String result1 = dbCall();
        String result2 = restCall();

//        Thread.sleep(Duration.ofMinutes(10));
        String result = String.format("[%s %s]", result1, result2);
        long end = System.currentTimeMillis();
        System.out.println("time =" + (end - start));

        System.out.println(result);
        return result;
    }

    private String dbCall() {
        NetworkCaller caller = new NetworkCaller("data");
        try {
            return caller.makeCall(2);
        } catch (Exception e) {
            return null;
        }
    }

    private String restCall() {
        NetworkCaller caller = new NetworkCaller("rest");
        try {
            return caller.makeCall(4);
        } catch (Exception e) {
            return null;
        }
    }

    private String externalCall() {
        try {
            NetworkCaller caller = new NetworkCaller("extn");
            return caller.makeCall(3);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

