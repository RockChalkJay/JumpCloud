package com.jumpcloud;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by jharris on 10/19/16.
 */
public class PasswordHashServiceTest {

    private static PasswordHashServiceImpl service;
    @BeforeClass
    public static void intializePasswordHashService() {
        service = new PasswordHashServiceImpl();
        //Calling createHashPassword here because it takes too long but bad practice
        try {
            service.createHashPassword("password1");
            service.createHashPassword("password2");
        } catch (UnsupportedEncodingException uee) {
            System.err.println(uee);
            //Fail the test
            assertTrue(false);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println(nsae);
            //Fail the test
            assertTrue(false);
        }

        try {
            Thread.sleep(10500);
        } catch (Exception e) {
            //Just eat the exception and move on
        }

        service.incrementNumberOfCalls();
        service.incrementNumberOfCalls();
    }

    @Test
    public void testCreateHashPassword() {

        ConcurrentHashMap<Long, String> jobIdPasswordHashMap = null;
        try {
            jobIdPasswordHashMap = (ConcurrentHashMap<Long, String>) getField(service, "jobIdPasswordHashMap");
        } catch (Exception e) {
            System.err.println(e);
            //Fail test
            assertTrue(false);
        }


        assertEquals("bc547750b92797f955b36112cc9bdd5cddf7d0862151d03a167ada8995aa24a9ad24610b36a68bc02da24141ee51670aea13ed6469099a4453f335cb239db5da",
                    jobIdPasswordHashMap.get(new Long(0)));


    }

    @Test
    public void testGetPasswordHash() {
        String hash = "";
        try {
            hash = service.getPasswordHash(new Long(0));
        } catch ( IOException io) {
            System.err.println(io);
            //Fail test
            assertTrue(false);
        }

        assertEquals("YmM1NDc3NTBiOTI3OTdmOTU1YjM2MTEyY2M5YmRkNWNkZGY3ZDA4NjIxNTFkMDNhMTY3YWRhODk5NWFhMjRhOWFkMjQ2MTBiMzZhNjhiYzAyZGEyNDE0MWVlNTE2NzBhZWExM2VkNjQ2OTA5OWE0NDUzZjMzNWNiMjM5ZGI1ZGE=",
                    hash);
    }

    @Test
    public void testGetStats() {

        service.incrementTotalResponseTime(8400001L);

        HashStats stats = service.getStats();

        assertEquals(2L, stats.getTotal());

        AtomicLong totalTime = null;
        try {
            totalTime = (AtomicLong) getField(service, "timeTracker");
        } catch (Exception e) {
            System.err.println(e);
            //Fail test
            assertTrue(false);
        }

        assertEquals(4.2, stats.getAverage());
    }

    @Test
    public void testGetStatsWithNoStats() {

        PasswordHashServiceImpl service1 = new PasswordHashServiceImpl();

        HashStats stats = service1.getStats();

        assertEquals(0L, stats.getTotal());
        assertEquals(0.0, stats.getAverage());
    }

    @Test
    public void testGetStatsWithNoTimer() {


        PasswordHashServiceImpl service1 = new PasswordHashServiceImpl();

        service1.incrementNumberOfCalls();
        HashStats stats = service1.getStats();

        assertEquals(1L, stats.getTotal());
        assertEquals(0.0, stats.getAverage());
    }

    @Test
    public void testGetStatsWithNoCalls() {


        PasswordHashServiceImpl service1 = new PasswordHashServiceImpl();

        service1.incrementTotalResponseTime(1234567890L);
        HashStats stats = service1.getStats();

        assertEquals(0L, stats.getTotal());
        assertEquals(0.0, stats.getAverage());
    }

    @Test
    public void testIncrementOfCalls() {
        AtomicLong calls = null;
        try {
            calls =  (AtomicLong) getField(service, "callCounter");
        } catch (Exception e) {
            System.err.println(e);
            //Fail test
            assertTrue(false);
        }

        //increment 2x in the init function
        service.incrementNumberOfCalls();
        assertEquals(3L, calls.get());
    }

    private Object getField( Object instance, String name ) throws Exception
    {
        Class c = instance.getClass();
        Field f = c.getDeclaredField( name );
        f.setAccessible( true );

        return f.get( instance );
    }

}
