package bio.terra.user.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.common.exception.ForbiddenException;
import bio.terra.common.iam.SamUser;
import bio.terra.common.sam.exception.SamNotFoundException;
import bio.terra.user.service.iam.SamService;
import bio.terra.user.testutils.BaseUnitTest;
import bio.terra.user.testutils.TestUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ProfileApiControllerTest extends BaseUnitTest {
  private static final String API = "/api/profile";

  @Autowired private MockMvc mockMvc;

  @MockBean private ProfileSamUserFactory userFactory;

  @MockBean private SamService samService;

  @Mock private SamUser user;

  @BeforeEach
  void beforeEach() {
    when(userFactory.from(any())).thenReturn(user);
    when(user.getSubjectId()).thenReturn(TestUtils.appendRandomNumber("fake"));
  }

  @Test
  void getUserProfile_emptyProfile() throws Exception {
    assertUserProfile("$.value", "");
    assertUserProfile("fake", "$.value", null);
  }

  @Test
  void setUserProfile_canSetProperty() throws Exception {
    setUserProfile("user.name.first", "{ \"value\": \"John\" }");
    assertUserProfile("$.value.user.name.first", "John");
  }

  @Test
  void getUserProfile_noValue() throws Exception {
    // row for the user now exists
    setUserProfile("user", "{ \"value\": \"v\" }");
    assertUserProfile("fake", "$.value", null);
  }

  @Test
  void multiUser() throws Exception {
    var user1 = TestUtils.appendRandomNumber("fake");
    var user2 = TestUtils.appendRandomNumber("fake");

    when(user.getSubjectId()).thenReturn(user1);
    setUserProfile("name", "{ \"value\": \"John\" }");
    assertUserProfile("$.value.name", "John");

    when(user.getSubjectId()).thenReturn(user2);
    assertUserProfile("$.value", "");
    setUserProfile("name", "{ \"value\": \"Mary\" }");
    assertUserProfile("$.value.name", "Mary");

    when(user.getSubjectId()).thenReturn(user1);
    assertUserProfile("$.value.name", "John");
  }

  @Test
  void getUserProfile_requestEmailSet_requesterNotAdmin_throws403() throws Exception {
    when(samService.adminGetUserIdByEmail(any(), any())).thenThrow(new ForbiddenException(""));
    setUserProfile("any", "{ \"value\": \"any\" }", "user.name@gmail.com", HttpStatus.SC_FORBIDDEN);
  }

  @Test
  void getUserProfile_requestEmailSet_emailNotFound_throws404() throws Exception {
    when(samService.adminGetUserIdByEmail(any(), any())).thenThrow(new SamNotFoundException(""));
    setUserProfile("any", "{ \"value\": \"any\" }", "user.name@gmail.com", HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void setUserProfile_requestEmailSet_requesterIsAdmin() throws Exception {
    var user1 = TestUtils.appendRandomNumber("fake");
    var user2 = TestUtils.appendRandomNumber("fake");

    when(user.getSubjectId()).thenReturn(user1);
    when(samService.adminGetUserIdByEmail(any(), any())).thenReturn(user2);
    setUserProfile("name", "{ \"value\": \"John\" }", "user2@gmail.com");

    when(user.getSubjectId()).thenReturn(user2);
    assertUserProfile("$.value.name", "John");
  }

  private void setUserProfile(String path, String value) throws Exception {
    setUserProfile(path, value, null);
  }

  private void setUserProfile(String path, String value, String userEmail) throws Exception {
    setUserProfile(path, value, userEmail, HttpStatus.SC_OK);
  }

  private void setUserProfile(String path, String value, String userEmail, int status)
      throws Exception {
    mockMvc
        .perform(
            put(API)
                .param("path", path)
                .param("userEmail", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value))
        .andExpect(status().is(status));
  }

  private void assertUserProfile(String jsonPath, Object value) throws Exception {
    assertUserProfile(null, jsonPath, value);
  }

  private void assertUserProfile(String apiPath, String jsonPath, Object value) throws Exception {
    mockMvc
        .perform(get(API).param("path", apiPath))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(jsonPath).value(value));
  }
}
