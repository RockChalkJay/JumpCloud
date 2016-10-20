package com.jumpcloud;

import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jharris on 10/19/16.
 */
@Service
public class PasswordHashServiceImpl implements PasswordHashService {

    private ConcurrentHashMap<Long, String> jobIdPasswordHashMap = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Future<HashResults>> futureHashQueue = new ConcurrentLinkedQueue();
    private AtomicLong jobIdCounter = new AtomicLong();
    private AtomicLong callCounter = new AtomicLong();
    private AtomicLong timeTracker = new AtomicLong();
    private ExecutorService executor;
    private boolean isShutdown = false;

    public PasswordHashServiceImpl() {
        executor = Executors.newFixedThreadPool(10);
        executor.execute(() -> {
            while(!isShutdown) {
                while(!futureHashQueue.isEmpty()) {
                    if (futureHashQueue.peek().isDone()) {
                        try {
                            HashResults result = futureHashQueue.poll().get();
                            if (result != null) {
                                jobIdPasswordHashMap.put(result.getJobId(), result.getHash());
                            }
                            //Only here for demo purposes --> I would leave out of production or log to debug file
                            System.out.println("Finished processing Job ID: " + result.getJobId());
                        } catch (ExecutionException ee) {
                            //Log error and move on
                            System.out.println(ee.toString());
                        } catch (InterruptedException ie) {
                            //Log error and move on
                            System.out.println(ie.toString());
                        }
                    } else if (futureHashQueue.peek().isCancelled()) {
                        //pop it off the queue and move on
                        futureHashQueue.remove();
                    }

                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        //Just eat the exception and move on
                    }
                }
            }
        });
    }

    @Override
    public long createHashPassword(String passwordToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        long jobId = jobIdCounter.getAndIncrement();

        Future<HashResults> future = executor.submit(() -> {

            //Wait 5 seconds per requirements
            try {
                Thread.sleep(5000);
            } catch(Exception e) {
                //Just eat the exception and move on
            }

            HashResults results = new HashResults(jobId);

            MessageDigest msgDigest = MessageDigest.getInstance("SHA-512");
            byte[] bytes = msgDigest.digest(passwordToHash.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            results.setHash(sb.toString());
            return results;
        });

        futureHashQueue.add(future);

        return jobId;
    }


    @Override
    public String getPasswordHash(long jobId) throws UnsupportedEncodingException {
        if(jobIdPasswordHashMap.get(jobId) == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(jobIdPasswordHashMap.get(jobId).getBytes("UTF-8"));
    }

    @Override
    public HashStats getStats() {
        double average;
        if (timeTracker.get() != 0 && callCounter.get() != 0 ) {
            average = (double) ((timeTracker.get() / callCounter.get()) / 1e6);
        } else {
            average = 0;
        }

        return new HashStats(callCounter.get(), roundToTwoPlaces(average));
    }

    @Override
    public void incrementNumberOfCalls() {
        callCounter.incrementAndGet();
    }

    @Override
    public void incrementTotalResponseTime(long callTimeInNanos) {
        timeTracker.addAndGet(callTimeInNanos);
    }

    @PreDestroy
    public void shutdown() {
        isShutdown = true;
        executor.shutdown(); // Disable new hashes from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor Service did not terminate");
            }
        } catch (InterruptedException ie) {
            // Re-Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private double roundToTwoPlaces(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class HashResults {

        private long jobId;
        private String hash;

        public HashResults(long jobId) {
            this.jobId = jobId;
        }

        public long getJobId() {
            return jobId;
        }

        public void setJobId(long jobId) {
            this.jobId = jobId;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }
    }
}
