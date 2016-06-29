package gov.dhs.uscis.td;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Utility class providing random string generation.
 */
public final class SessionIdGenerator {
  private static SecureRandom random = new SecureRandom();

  public static String nextSessionId() {
    return new BigInteger(130, random).toString(32);
  }
}
