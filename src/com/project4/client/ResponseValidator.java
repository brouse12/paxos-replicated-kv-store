package com.project4.client;

import java.util.function.Predicate;

/**
 * Includes a series of Predicate classes for evaluating an acknowledgement received from the
 * server.  Any test method will return true if the ack response indicates that the corresponding
 * operation was successful.
 */
class ResponseValidator {

  // Validates server's PUT response
  static class ValidatePutResponse implements Predicate<String> {
    private String key;
    private String value;

    ValidatePutResponse(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean test(String s) {
      String[] respTokens = s.split(" ");
      return respTokens.length == 3 &&
              respTokens[0].equals("PUT") &&
              respTokens[1].equals(key) &&
              respTokens[2].equals(value);
    }
  }

  // Validates server's GET response
  static class ValidateGetResponse implements Predicate<String> {
    private String key;

    ValidateGetResponse(String key) {
      this.key = key;
    }

    @Override
    public boolean test(String s) {
      String[] respTokens = s.split(" ");
      return respTokens.length == 5 &&
              respTokens[0].equals("GET") &&
              respTokens[1].equals("KEY:") &&
              respTokens[2].equals(key) &&
              respTokens[3].equals("VAL:");
    }
  }

  // Validates server's DELETE response
  static class ValidateDelResponse implements Predicate<String> {
    private String key;

    ValidateDelResponse(String key) {
      this.key = key;
    }

    @Override
    public boolean test(String s) {
      String[] respTokens = s.split(" ");
      return respTokens.length == 2 &&
              respTokens[0].equals("DEL") &&
              respTokens[1].equals(key);
    }
  }
}
