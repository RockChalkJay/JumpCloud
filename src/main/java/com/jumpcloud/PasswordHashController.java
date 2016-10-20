package com.jumpcloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jharris on 10/19/16.
 */
@RestController
public class PasswordHashController {

    private PasswordHashService passwordHashService;

    @Autowired
    public PasswordHashController(PasswordHashService passwordHashService) {
        this.passwordHashService = passwordHashService;
    }

    @RequestMapping(value = "/hash", method = RequestMethod.POST)
    public ResponseEntity<String> hashPassword(@RequestBody String password) {

        passwordHashService.incrementNumberOfCalls();
        long startTime = System.nanoTime();


        if (password.isEmpty()) {
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            return new ResponseEntity<String>("Excepting password=<my_password>", HttpStatus.BAD_REQUEST);
        } else if ( password.length() >= 10 ) {
            if(password.substring(0,9).equals("password=")) {
                password = password.substring(9);
            } else {
                passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
                return new ResponseEntity<String>("Excepting password=<my_password>", HttpStatus.BAD_REQUEST);
            }
        } else {
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            return new ResponseEntity<String>("Excepting password=<my_password>", HttpStatus.BAD_REQUEST);
        }

        long jobId = 0;
        try {
            jobId = passwordHashService.createHashPassword(password);
        } catch(UnsupportedEncodingException uee) {
            //output exception
            System.err.println(uee.toString());
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NoSuchAlgorithmException nsae) {
            //output exception
            System.err.println(nsae.toString());
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
        return new ResponseEntity<String>(Long.toString(jobId), HttpStatus.OK);
    }

    @RequestMapping(value = "/hash/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getHashPassword(@PathVariable("id") long id) {
        passwordHashService.incrementNumberOfCalls();
        long startTime = System.nanoTime();

        try {
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            String hash = passwordHashService.getPasswordHash(id);
            if(hash != null && !hash.isEmpty())
                return new ResponseEntity<String>(hash, HttpStatus.OK);
            {
                return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(UnsupportedEncodingException uee) {
            //output exception
            System.err.println(uee.toString());
            passwordHashService.incrementTotalResponseTime(System.nanoTime() - startTime);
            return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/stats", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<HashStats> getStats() {

        return new ResponseEntity<HashStats>(passwordHashService.getStats(), HttpStatus.OK);
    }
}
