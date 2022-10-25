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
import bio.terra.user.service.iam.SamService;
import bio.terra.user.testutils.BaseUnitTest;
import bio.terra.user.testutils.TestUtils;
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
  void getEmptyProfile() throws Exception {
    assertUserProfile("$.value", "");
    assertUserProfile("fake", "$.value", null);
  }

  @Test
  void setProperty() throws Exception {
    setUserProfile("user.name.first", "{ \"value\": \"John\" }");
    assertUserProfile("$.value.user.name.first", "John");
  }

  @Test
  void nonEmptyProfileNoValue() throws Exception {
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
  void adminNoPermission() throws Exception {
    when(samService.adminGetUserIdByEmail(any(), any())).thenThrow(new ForbiddenException(""));
    mockMvc
        .perform(
            put(API)
                .param("path", "any")
                .param("userEmail", "user.name@gmail.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"value\": \"any\" }"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminSet() throws Exception {
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
    mockMvc
        .perform(
            put(API)
                .param("path", path)
                .param("userEmail", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value))
        .andExpect(status().isOk());
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
