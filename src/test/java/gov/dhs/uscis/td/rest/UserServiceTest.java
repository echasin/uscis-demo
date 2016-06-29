package gov.dhs.uscis.td.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import gov.dhs.uscis.td.DatabaseConnector;
import gov.dhs.uscis.td.domain.User;
import spark.Request;
import spark.Response;
import spark.Session;

public class UserServiceTest {

  private UserService service;
  private DatabaseConnector mockDb;
  private Request mockReq;
  private Response mockRes;
  private Session mockSession;

  private static final String SESSION_KEY = "somekey";
  private static final String USERNAME = "userName";
  private static final String DEFAULT_USERNAME = "default_user";
  private static final User user = new User("username", "password");

  @Before
  public void setup() {
    mockDb = mock(DatabaseConnector.class);
    mockReq = mock(Request.class);
    mockRes = mock(Response.class);
    mockSession = mock(Session.class);
    service = new UserService(mockDb);
  }

  @Test
  public void getAuthorFromRequestLoggedIn() throws Exception {
    when(mockReq.session(false)).thenReturn(mockSession);
    when(mockSession.attribute(UserService.SESSION_KEY_NAME)).thenReturn(SESSION_KEY);
    when(mockDb.isSessionValid(SESSION_KEY)).thenReturn(true);
    when(mockDb.getUsernameFromSessionKey(SESSION_KEY)).thenReturn(USERNAME);

    assertEquals(USERNAME, service.getAuthorFromRequest(mockReq));
  }

  @Test
  public void getAuthorFromRequestNotLoggedIn() throws Exception {
    when(mockReq.session(false)).thenReturn(null);

    assertEquals(DEFAULT_USERNAME, service.getAuthorFromRequest(mockReq));
  }

  @Test
  public void loginAlreadyLoggedIn() throws Exception {
    when(mockReq.session(false)).thenReturn(mockSession);
    when(mockSession.attribute(UserService.SESSION_KEY_NAME)).thenReturn(SESSION_KEY);

    service.login(mockReq, mockRes, user);

    verify(mockRes).redirect("/");
  }

  @Test
  public void loginSuccessfully() throws Exception {
    when(mockReq.session(false)).thenReturn(null);
    when(mockReq.session()).thenReturn(mockSession);
    when(mockDb.lookupUser(user)).thenReturn(true);
    when(mockDb.newSessionKeyForUser(user)).thenReturn(SESSION_KEY);

    service.login(mockReq, mockRes, user);

    verify(mockSession).attribute(UserService.SESSION_KEY_NAME, SESSION_KEY);
  }

  @Test
  public void loginNoUser() throws Exception {
    when(mockReq.session(false)).thenReturn(null);
    when(mockDb.lookupUser(user)).thenReturn(false);

    service.login(mockReq, mockRes, user);

    verify(mockRes).status(401);
  }

  @Test
  public void logoutNotLoggedIn() throws Exception {
    when(mockReq.session(false)).thenReturn(null);

    service.logout(mockReq, mockRes);

    verify(mockRes).redirect("/");
  }

  @Test
  public void logoutSuccessfully() throws Exception {
    when(mockReq.session(false)).thenReturn(mockSession);
    when(mockSession.attribute(UserService.SESSION_KEY_NAME)).thenReturn(SESSION_KEY);

    service.logout(mockReq, mockRes);

    verify(mockDb).terminateSessionsForUserWithKey(SESSION_KEY);
  }
}
