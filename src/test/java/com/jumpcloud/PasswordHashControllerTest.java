package com.jumpcloud;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by jharris on 10/19/16.
 */
public class PasswordHashControllerTest {

    private PasswordHashController controller;
    private PasswordHashService service;
    @Before
    public void initializeContoller() {
        service = Mockito.mock(PasswordHashService.class);
        controller = new PasswordHashController(service);
    }

    @Test
    public void passwordHashTest() throws NoSuchAlgorithmException, UnsupportedEncodingException{
        when(service.createHashPassword("password1")).thenReturn(1L);

        ResponseEntity<String> response = controller.hashPassword("password=password1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1", response.getBody());
        verify(service, times(1)).createHashPassword("password1");
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void passwordHashWithBadPasswordFormatTest() throws NoSuchAlgorithmException, UnsupportedEncodingException{
        ResponseEntity<String> response = controller.hashPassword("password1=password");
        try {
            Thread.sleep(600);
        } catch(InterruptedException ie) {
            //just eat it and move on
        }
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(service, times(0)).createHashPassword(anyString());
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void passwordHashWithEmptyPasswordTest() throws NoSuchAlgorithmException, UnsupportedEncodingException{
        ResponseEntity<String> response = controller.hashPassword("password=");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        verify(service, times(0)).createHashPassword(anyString());
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void passwordHashWithEmptyStringTest() throws NoSuchAlgorithmException, UnsupportedEncodingException{
        ResponseEntity<String> response = controller.hashPassword("");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        verify(service, times(0)).createHashPassword(anyString());
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void passwordHashAlgorithException() throws NoSuchAlgorithmException {

            try {
                when(service.createHashPassword("password1")).thenThrow(NoSuchAlgorithmException.class);
            } catch(UnsupportedEncodingException e) {
                assertTrue(false);
            }

            ResponseEntity<String> response = controller.hashPassword("password=password1");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            try {
                verify(service, times(1)).createHashPassword("password1");
            } catch (UnsupportedEncodingException e) {
                assertTrue(false);
            }
            verify(service, times(1)).incrementNumberOfCalls();
            verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void passwordHashUnsupportedEncodingException() throws UnsupportedEncodingException {

        try {
            when(service.createHashPassword("password1")).thenThrow(UnsupportedEncodingException.class);
        } catch(NoSuchAlgorithmException e) {
            assertTrue(false);
        }

        ResponseEntity<String> response = controller.hashPassword("password=password1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        try {
            verify(service, times(1)).createHashPassword("password1");
        } catch (NoSuchAlgorithmException e) {
            assertTrue(false);
        }
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void getPasswordHashTest() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        when(service.getPasswordHash(1L)).thenReturn("Ha$H");

        ResponseEntity<String> response = controller.getHashPassword(1L);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), "Ha$H");
        verify(service, times(1)).getPasswordHash(1L);
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    @Test
    public void getPasswordHashNotFoundTest() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        when(service.getPasswordHash(1L)).thenReturn(null);

        ResponseEntity<String> response = controller.getHashPassword(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(response.getBody(), "");
        verify(service, times(1)).getPasswordHash(1L);
        verify(service, times(1)).incrementNumberOfCalls();
        verify(service, times(1)).incrementTotalResponseTime(anyLong());
    }

    //Exception test

    @Test
    public void getStats() {
        service = Mockito.mock(PasswordHashService.class);
        controller = new PasswordHashController(service);

        HashStats stats = new HashStats(42, 55.56);
        when(service.getStats()).thenReturn(stats);

        ResponseEntity<HashStats> response = controller.getStats();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals( stats, response.getBody());
        verify(service, times(0)).incrementNumberOfCalls();
        verify(service, times(0)).incrementTotalResponseTime(anyLong());
    }
}
