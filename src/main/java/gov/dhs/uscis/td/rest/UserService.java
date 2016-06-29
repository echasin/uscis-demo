package gov.dhs.uscis.td.rest;

import gov.dhs.uscis.td.DatabaseConnector;
import gov.dhs.uscis.td.domain.User;
import spark.Request;
import spark.Response;
import spark.Session;

import java.sql.SQLException;

public class UserService {

  public static final String SESSION_KEY_NAME = "SESSION_KEY";
  private DatabaseConnector db;

  public UserService(DatabaseConnector db) {
    this.db = db;
  }

  /**
   * Return the author of a post using the session stored in the {@link spark.Request}.
   * "default_user" is the default username for users not logged in.
   * 
   * @param req The {@link spark.Request} holding the session key.
   */
  public String getAuthorFromRequest(Request req) throws SQLException {
    String author = "";

    // Get the session only if it already existed.
    Session session = req.session(false);
    boolean isLoggedIn = false;
    String sessionKey = "";

    // Check if user is logged in.
    if (session != null && !((String) session.attribute(SESSION_KEY_NAME)).isEmpty()) {
      sessionKey = session.attribute(SESSION_KEY_NAME);
      isLoggedIn = db.isSessionValid(sessionKey);
    }

    // Set correct author.
    author = isLoggedIn ? db.getUsernameFromSessionKey(sessionKey) : "default_user";
    return author;
  }

  /**
   * Logs a user into the system, validating credentials and creating a session for them.
   * 
   * @param req The {@link spark.Request} from the user, to get and set the session object.
   * @param res The {@link spark.Response} given back to the user, for redirects.
   * @param user The {@link User} POJO storing credential data to be verified.
   */
  public Response login(Request req, Response res, User user) throws SQLException {
    // Get the session only if it already existed.
    Session session = req.session(false);

    // If user is already logged in, do nothing.
    if (session != null && !((String) session.attribute(SESSION_KEY_NAME)).isEmpty()) {
      res.redirect("/");
      return res;
    }

    // Validate credentials and generate a session key.
    if (db.lookupUser(user)) {
      // Add the session key to the Request's session.
      String sessionKey = db.newSessionKeyForUser(user);
      req.session().attribute(SESSION_KEY_NAME, sessionKey);
    } else {
      // Unauthorized access.
      res.status(401);
    }
    return res;
  }

  /**
   * Logs a user out of their current session by remoing their session object from the database.
   * 
   * @param req The {@link spark.Request} from the user, to get and set the session object.
   * @param res The {@link spark.Response} given back to the user, for redirects.
   */
  public Response logout(Request req, Response res) throws SQLException {
    // Get the session only if it already existed.
    Session session = req.session(false);

    // Ensure session exists, then terminate each of this user's sessions.
    if (session != null && !((String) session.attribute(SESSION_KEY_NAME)).isEmpty()) {
      db.terminateSessionsForUserWithKey(session.attribute(SESSION_KEY_NAME));
    }

    // Redirect to default guestbook.
    res.redirect("/");
    return res;
  }
}
