package gov.dhs.uscis.td;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;

import com.google.gson.Gson;

import gov.dhs.uscis.td.domain.Greeting;
import gov.dhs.uscis.td.domain.User;
import gov.dhs.uscis.td.rest.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

  /**
   * Main method that starts the server.
   */
  public static void main(String[] args) {
    Config config = new Config();
    DatabaseConnector db = new DatabaseConnector(config);
    UserService userService = new UserService(db);
    Gson gson = new Gson();

    port(config.getPort());
    staticFileLocation("/public");

    // GET endpoint for retrieving messages by guestbook.
    get("/api/v1/messages/:guestbook_name", (req, res) -> {
      Map<String, Object> attributes = new HashMap<>();
      List<Greeting> output = db.getMessages(req.params(":guestbook_name"));
      attributes.put("results", output);

      return gson.toJson(attributes);
    });

    // POST endpoint for posting a new message to a guestbook.
    post("/api/v1/messages/:guestbook_name", (req, res) -> {
      Map<String, Object> attributes = new HashMap<>();

      // Get the appropriate author using the request.
      String author = userService.getAuthorFromRequest(req);

      // Get message, set the correct author, and store it in the database.
      Greeting messageFromPost = gson.fromJson(req.body(), Greeting.class);
      Greeting message = new Greeting(messageFromPost.getMessage(), messageFromPost.getDate(),
          author, req.params(":guestbook_name"));
      List<Greeting> output = db.putMessage(message);

      attributes.put("results", output);
      return gson.toJson(attributes);
    });

    // POST endpoint for registering a new user.
    post("/api/v1/register", (req, res) -> {
      User user = gson.fromJson(req.body(), User.class);
      db.createUser(user);
      return "";
    });

    // POST endpoint for logging in an existing user.
    post("/api/v1/login", (req, res) -> {
      User user = gson.fromJson(req.body(), User.class);
      return userService.login(req, res, user);
    });

    // GET endpoint for terminating a user's current session.
    get("/api/v1/logout", (req, res) -> {
      return userService.logout(req, res);
    });
  }
}
