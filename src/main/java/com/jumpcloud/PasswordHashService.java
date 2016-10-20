package com.jumpcloud;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jharris on 10/19/16.
 */
public interface PasswordHashService {

    public long createHashPassword(String passwordToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException;

    public String getPasswordHash(long jobId) throws UnsupportedEncodingException;

    public HashStats getStats();

    public void incrementNumberOfCalls();

    public void incrementTotalResponseTime(long callTime);
}
