package gov.dhs.uscis.td;

import gov.dhs.uscis.td.domain.Greeting;
import gov.dhs.uscis.td.domain.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class that connects to and queries the database.
 */
public class DatabaseConnector {

  private String dbUrl;

  public DatabaseConnector(Config config) {
    dbUrl = config.getDatabaseUrl();
    setupDatabase();
  }

  private void setupDatabase() {
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Create the "guestbook" table and a "default" guestbook, if they do not exist.
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS guestbook (id SERIAL PRIMARY KEY, book text)");
      stmt.executeUpdate("INSERT INTO guestbook (book)" + "SELECT 'default' " + "WHERE NOT EXISTS "
          + "(SELECT id FROM guestbook WHERE id = 1)");

      // Create the "user" table and a "defalt_user", if they do not exist.
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users "
          + "(id SERIAL PRIMARY KEY, uname text UNIQUE, password text)");
      stmt.executeUpdate(
          "INSERT INTO users (uname, password)" + "SELECT 'default_user', 'password' "
              + "WHERE NOT EXISTS" + "(SELECT id FROM users WHERE id = 1)");

      // Create the "messages" table if it does not exist.
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages ("
          + "id SERIAL PRIMARY KEY, msg text, tick timestamp, "
          + "guestbook_id integer NOT NULL REFERENCES guestbook (id), "
          + "user_id integer NOT NULL REFERENCES users (id))");

      // Create the "sessions" table if it does not exist.
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS sessions (" + "id SERIAL PRIMARY KEY, session_key text, "
              + "user_id integer NOT NULL REFERENCES users (id))");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Close the connection regardless of what else happens.
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void setupDatabaseForTests() {
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Add test messages to the "messages" table.
      stmt.executeUpdate("INSERT INTO messages (msg, tick, guestbook_id, user_id) "
          + "SELECT 'test message', now(), 1, 1 "
          + "WHERE NOT EXISTS (SELECT id FROM messages WHERE id = 1)");
      stmt.executeUpdate("INSERT INTO messages (msg, tick, guestbook_id, user_id) "
          + "SELECT 'second test message', now(), 1, 1 "
          + "WHERE NOT EXISTS (SELECT id FROM messages WHERE id = 2)");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Close the connection regardless of what else happens.
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void teardownDatabase() {
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Drop all data and tables in the database.
      stmt.executeUpdate("DROP SCHEMA public CASCADE");
      stmt.executeUpdate("CREATE SCHEMA public");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Close the connection regardless of what else happens.
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Returns a list of {@link Greeting}s for a given guestbook.
   * 
   * @param guestbookName The name of the guestbook.
   */
  public List<Greeting> getMessages(String guestbookName) throws SQLException {
    Connection connection = null;
    ArrayList<Greeting> output = new ArrayList<>();

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Get all messages in descending time order.
      ResultSet rs = stmt.executeQuery(
          "SELECT msg, tick, uname FROM messages JOIN users ON messages.user_id=users.id "
              + "JOIN guestbook ON messages.guestbook_id=guestbook.id WHERE book='"
              + guestbookName + "' ORDER BY tick DESC");
      // Create a Greeting POJO for each result and add it to the output list.
      while (rs.next()) {
        output.add(new Greeting(rs.getString("msg"), rs.getTimestamp("tick"), rs.getString("uname"),
            guestbookName));
      }
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return output;
  }

  /**
   * Adds a {@link Greeting} to a guestbook, and returns the list of all messages for that
   * guestbook.
   * 
   * @param message The {@link Greeting} to add.
   */
  public List<Greeting> putMessage(Greeting message) throws SQLException {
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      Integer guestbookId = getOrCreateGuestbookId(message.getGuestbook(), connection);
      Integer authorId = getUserId(message.getAuthor(), connection);
      stmt.execute("INSERT INTO messages (msg, tick, guestbook_id, user_id) VALUES ('"
          + message.getMessage() + "', now()," + guestbookId + ", " + authorId + ")");
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return getMessages(message.getGuestbook());
  }

  /**
   * Creates a new {@link User} in the system using the given username and password.
   * 
   * @param user The {@link User} POJO holding the username and password to store.
   * @return The new user's unique ID in the "users" table.
   */
  public int createUser(User user) throws SQLException {
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Insert the new user into the "users" table.
      stmt.executeUpdate("INSERT INTO users (uname, password) VALUES ('" + user.getUsername()
          + "', '" + user.getPassword() + "')");
      ResultSet rs =
          stmt.executeQuery("SELECT id FROM users WHERE uname = '" + user.getUsername() + "'");
      rs.next();
      return rs.getInt("id");
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  /**
   * Given a {@link User}'s credentials, returns whether that user exists in the system.
   * 
   * @param user The {@link User} POJO holding the username and password to validate.
   */
  public boolean lookupUser(User user) throws SQLException {
    Connection connection = null;
    boolean userExists = false;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Find a user with both matching username and password.
      ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE uname = '" + user.getUsername()
          + "' AND password = '" + user.getPassword() + "'");
      if (rs.next()) {
        userExists = true;
      }
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return userExists;
  }

  /**
   * Creates a new session for the given {@link User}.
   * 
   * @param user The {@link User} to link the created session to.
   */
  public String newSessionKeyForUser(User user) throws SQLException {
    String sessionKey = SessionIdGenerator.nextSessionId();
    Connection connection = null;
    int userId;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Get the user's unique ID and link the Session key to it in the database
      userId = getUserId(user.getUsername(), connection);
      stmt.executeUpdate("INSERT INTO sessions (session_key, user_id) VALUES ('" + sessionKey
          + "', " + userId + ")");
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return sessionKey;
  }

  /**
   * Terminates a particular session and all other sessions that {@link User} might be linked to.
   * 
   * @param sessionKey The key to the session being terminated.
   */
  public void terminateSessionsForUserWithKey(String sessionKey) throws SQLException {
    Connection connection = null;
    int userId;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Get the user's unique ID. If the user doesn't exist, they won't have sessions to delete.
      ResultSet rs = stmt
          .executeQuery("SELECT user_id FROM sessions WHERE session_key = '" + sessionKey + "'");
      if (!rs.next()) {
        return;
      }

      // Delete all sessions from the database related to this user.
      userId = rs.getInt("user_id");
      stmt.execute("DELETE FROM sessions WHERE user_id = " + userId);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  /**
   * Returns true if the session key provided maps to a live session for a user in the system.
   * 
   * @param sessionKey The session key to check.
   */
  public boolean isSessionValid(String sessionKey) throws SQLException {
    boolean isSessionValid = false;
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Query the database to see if the session exists.
      ResultSet rs = stmt
          .executeQuery("SELECT user_id FROM sessions WHERE session_key = '" + sessionKey + "'");
      if (rs.next()) {
        isSessionValid = true;
      }
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return isSessionValid;
  }

  /**
   * Given a session key, return the name of the {@link User} tied to that session.
   * 
   * @param sessionKey The session key used to find the attached user.
   */
  public String getUsernameFromSessionKey(String sessionKey) throws SQLException {
    String username = "";
    Connection connection = null;

    try {
      connection = DriverManager.getConnection(dbUrl);
      Statement stmt = connection.createStatement();

      // Cross-query the "sessions" and "users" tables to get username via session key.
      ResultSet rs =
          stmt.executeQuery("SELECT uname FROM sessions JOIN users ON sessions.user_id = users.id "
              + "WHERE sessions.session_key = '" + sessionKey + "'");
      if (rs.next()) {
        username = rs.getString("uname");
      }
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return username;
  }

  private int getOrCreateGuestbookId(String guestbookName, Connection connection)
      throws SQLException {
    Statement stmt = connection.createStatement();
    int guestbookId;
    String guestbookQuery = "SELECT id FROM guestbook WHERE book = '" + guestbookName + "'";

    // Check that the guestbook exists, get it's ID.
    ResultSet rs = stmt.executeQuery(guestbookQuery);
    if (rs.next()) {
      guestbookId = rs.getInt("id");
    } else {
      // Otherwise, create the guestbook.
      stmt.executeUpdate("INSERT INTO guestbook (book) VALUES ('" + guestbookName + "')");
      rs = stmt.executeQuery(guestbookQuery);
      rs.next();
      guestbookId = rs.getInt("id");
    }

    return guestbookId;
  }

  private int getUserId(String userName, Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    int userId;

    // Query for the user by username, return it's unique ID.
    ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE uname = '" + userName + "'");
    if (rs.next()) {
      userId = rs.getInt("id");
    } else {
      // Throw an error that user was not found.
      throw new SQLException("User not found.");
    }

    return userId;
  }
}
