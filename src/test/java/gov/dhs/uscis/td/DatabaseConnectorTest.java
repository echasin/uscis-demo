package gov.dhs.uscis.td;

import gov.dhs.uscis.td.domain.User;
import org.junit.*;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import gov.dhs.uscis.td.domain.Greeting;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class DatabaseConnectorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private DatabaseConnector db;
  private User testUser = new User("user", "secure");
  private final String DATABASE_URL = "jdbc:postgresql:///demo_test";

  private class MockConfig extends Config {
    public String getDatabaseUrl() {
      return DATABASE_URL;
    }
  }

  @Before
  public void setUp() throws Exception {
    if (db == null) {
      MockConfig testenv = new MockConfig();
      db = new DatabaseConnector(testenv);
    }
    db.setupDatabaseForTests();
  }

  @After
  public void tearDown() throws Exception {
    db.teardownDatabase();
  }

  @Test
  public void getMessages() throws Exception {
    // Note: See DatabaseConnector.setupDatabaseForTest().
    List<Greeting> msgs = db.getMessages("default");
    assertEquals(2, msgs.size());

    Greeting mostRecentMsg = msgs.get(0);
    assertEquals("second test message", mostRecentMsg.getMessage());
    assertEquals("default_user", mostRecentMsg.getAuthor());
    assertEquals("default", mostRecentMsg.getGuestbook());
  }

  @Test
  public void putMessage() throws Exception {
    // Note: See DatabaseConnector.setupDatabaseForTest().
    Greeting greeting = new Greeting("Test", new Date(), "default_user", "default");
    List<Greeting> putOutput = db.putMessage(greeting);
    List<Greeting> msgs = db.getMessages("default");

    assertEquals(msgs, putOutput);
    assertEquals(3, msgs.size());
    String mostRecentMsg = msgs.get(0).getMessage();
    assertEquals("Test", mostRecentMsg);

    Greeting newGuestbookGreeting = new Greeting("Test", new Date(), "default_user", "new");
    putOutput = db.putMessage(newGuestbookGreeting);
    List<Greeting> newMsgs = db.getMessages("new");

    assertEquals(newMsgs, putOutput);
    assertEquals(1, newMsgs.size());
    assertEquals("new", newMsgs.get(0).getGuestbook());
  }

  @Test
  public void putMessageFailOnUser() throws Exception {
    Greeting greeting = new Greeting("Test", new Date(), "failing user", "default");
    exception.expect(SQLException.class);
    db.putMessage(greeting);
  }

  @Test
  public void createUser() throws Exception {
    Integer userId = db.createUser(testUser);
    assertTrue(userId != null);

    // Verify that the user was added to the db
    Connection connection = DriverManager.getConnection(DATABASE_URL);
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM users "
        + "WHERE id = '" + userId + "'");
    assertTrue(rs.next());
    assertEquals(rs.getString("uname"), testUser.getUsername());
    connection.close();
  }

  @Test
  public void duplicateUserFail() throws Exception {
    Integer userId = db.createUser(testUser);
    assertTrue(userId != null);

    exception.expect(SQLException.class);
    db.createUser(testUser);
  }

  @Test
  public void lookupUserSuccess() throws Exception {
    db.createUser(testUser);
    assertTrue(db.lookupUser(testUser));
  }

  @Test
  public void lookupUserFailure() throws Exception {
    assertFalse(db.lookupUser(testUser));
  }

  @Test
  public void newSessionSuccess() throws Exception {
    // Create user for test
    db.createUser(testUser);

    // Call to create the new session
    String sessionKey = db.newSessionKeyForUser(testUser);

    // Verify that the session key returned is the same as what's in the db
    Connection connection = DriverManager.getConnection(DATABASE_URL);
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM sessions JOIN users "
        + "ON sessions.user_id = users.id WHERE sessions.session_key = '" + sessionKey + "'");
    assertTrue(rs.next());
    assertEquals(rs.getString("uname"), testUser.getUsername());
    connection.close();
  }

  @Test
  public void terminateSessionsSuccess() throws Exception {
    // Create user, session for that user
    int userId = db.createUser(testUser);
    String sessionKey = db.newSessionKeyForUser(testUser);

    // Terminate all sessions for user
    db.terminateSessionsForUserWithKey(sessionKey);

    // Verify sessions no longer exist for this user
    Connection connection = DriverManager.getConnection(DATABASE_URL);
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM sessions JOIN users "
        + "ON sessions.user_id = users.id WHERE users.id=" + userId);
    assertFalse(rs.next());
    connection.close();
  }

  @Test
  public void isValidSession() throws Exception {
    // Create user, session for that user
    db.createUser(testUser);
    String sessionKey = db.newSessionKeyForUser(testUser);

    assertTrue(db.isSessionValid(sessionKey));
    db.terminateSessionsForUserWithKey(sessionKey);
    assertFalse(db.isSessionValid(sessionKey));
  }

  @Test
  public void getUserFromSessionKey() throws Exception {
    // Create user and a couple sessions for test users
    User testUser2 = new User("testUser2", "hunter2");
    db.createUser(testUser);
    db.createUser(testUser2);
    String sessionKey = db.newSessionKeyForUser(testUser);
    String sessionKey2 = db.newSessionKeyForUser(testUser2);

    assertEquals(testUser.getUsername(), db.getUsernameFromSessionKey(sessionKey));
    assertEquals(testUser2.getUsername(), db.getUsernameFromSessionKey(sessionKey2));
    assertNotEquals(testUser.getUsername(), db.getUsernameFromSessionKey(sessionKey2));
    assertNotEquals(testUser2.getUsername(), db.getUsernameFromSessionKey(sessionKey));
  }
}
