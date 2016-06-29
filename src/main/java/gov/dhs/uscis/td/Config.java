package gov.dhs.uscis.td;

/**
 * Configuration utility class for getting System environment variables. Allows for easier testing
 * of classes dependent on environment variables.
 */
public class Config {
  public String getDatabaseUrl() {
    return System.getenv("JDBC_DATABASE_URL");
  }

  public int getPort() {
    return Integer.valueOf(System.getenv("PORT"));
  }
}
