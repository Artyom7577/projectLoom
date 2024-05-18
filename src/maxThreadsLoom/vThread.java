package maxThreadsLoom;

import java.lang.Thread.Builder.OfVirtual;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class vThread {
    private static void handleUserRequest() {
        System.out.println("Starting Thread" + Thread.currentThread());
        try {
            Thread.sleep(2000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ending Thread" + Thread.currentThread());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Main");

//        List<Thread> threads = new ArrayList<>();
//        for (int i = 0; i < 4000; i++) {
//            threads.add(startThread());
////            startPlatformThread();
//        }
//
//        // join on the threads
//        for (Thread thread: threads) {
//            thread.join();
//        }

//        playWithVirtualBuilderThreadSave();

        createVThreadWithExecutorService();
        System.out.println("Ending Main");


    }

    public static void playWithVirtualBuilder() throws  Exception {
        OfVirtual vBuilder = Thread.ofVirtual().name("userThread", 0);

        Thread vThread1 = vBuilder.start(vThread::handleUserRequest);
        Thread vThread2 = vBuilder.start(vThread::handleUserRequest);

        vThread1.join();
        vThread2.join();

        //builder is not thread safe
    }

    public static void playWithVirtualBuilderThreadSave() throws Exception {
        ThreadFactory factory = Thread.ofVirtual().name("userThread", 0).factory();


        Thread vThread1 = factory.newThread(vThread::handleUserRequest);
        vThread1.start();
        Thread vThread2 = factory.newThread(vThread::handleUserRequest);
        vThread2.start();

        vThread1.join();
        vThread2.join();

        //thread safe
    }
    private static Thread startThread() {
        return Thread.startVirtualThread(vThread::handleUserRequest);
    }

    private static void startPlatformThread() {
        new Thread(vThread::handleUserRequest).start();
    }


    // use executor service

    public static void createVThreadWithExecutorService() {
        ThreadFactory factory = Thread.ofVirtual().name("userThread", 0).factory();
        try(ExecutorService srv = Executors.newThreadPerTaskExecutor(factory)) {
            srv.submit(vThread::handleUserRequest);
            srv.submit(vThread::handleUserRequest);
        }
    }
}
