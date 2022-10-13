package bio.terra.user.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.common.iam.SamUser;
import bio.terra.user.testutils.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ProfileApiControllerTest extends BaseUnitTest {
  private static final String API = "/api/profile";

  @Autowired private MockMvc mockMvc;

  @MockBean private ProfileSamUserFactory userFactory;

  private final SamUser user = mock(SamUser.class);

  @BeforeEach
  void beforeEach() {
    when(userFactory.from(any())).thenReturn(user);
    var id = String.valueOf(Math.random()).substring(2);
    when(user.getSubjectId()).thenReturn("fake-" + id);
  }

  @Test
  void getEmptyProfile() throws Exception {
    mockMvc.perform(get(API)).andExpect(status().isNotFound());
  }

  @Test
  void recursiveSet() throws Exception {
    setUserProfile("user.name.first", "{ \"value\": \"John\" }");
    assertUserProfile("$.value.user.name.first", "John");
  }

  @Test
  void setSideBySide() throws Exception {
    setUserProfile("settings.darkmode", "{ \"value\": true }");
    setUserProfile("settings.language", "{ \"value\": \"en\" }");

    assertUserProfile("$.value.settings.darkmode", true);
    assertUserProfile("$.value.settings.language", "en");
  }

  @Test
  void clobberSet() throws Exception {
    setUserProfile("starred", "{ \"value\": [\"workspace1\", \"workspace2\"] }");
    assertUserProfile("$.value.starred", new String[] { "workspace1", "workspace2" });

    setUserProfile("starred", "{ \"value\": null }");
    assertUserProfile(".value.starred", "");
  }

  private void setUserProfile(String path, String value) throws Exception {
    mockMvc
        .perform(
            put(API).param("path", path).contentType(MediaType.APPLICATION_JSON).content(value))
        .andExpect(status().isNoContent());
  }

  private void assertUserProfile(String jsonPath, Object value) throws Exception {
    mockMvc
        .perform(get(API))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(jsonPath).value(value));
  }
}
