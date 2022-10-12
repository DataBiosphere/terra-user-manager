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
    String json = "{ \"value\": \"John\" }";

    mockMvc
        .perform(
            put(API)
                .param("path", "user.name.first")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(API))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.value.user.name.first").value("John"));
  }

  @Test
  void setSideBySide() throws Exception {
    String json = "{ \"value\": true }";

    mockMvc
        .perform(
            put(API)
                .param("path", "settings.darkmode")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isNoContent());

    String json2 = "{ \"value\": \"en\" }";

    mockMvc
        .perform(
            put(API)
                .param("path", "settings.language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(API))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.value.settings.darkmode").value(true))
        .andExpect(jsonPath("$.value.settings.language").value("en"));
  }

  @Test
  void overwrite() throws Exception {
    String json = "{ \"value\": {\"a\": true, \"b\": false } }";

    mockMvc
        .perform(put(API).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isNoContent());

    String json2 = "{ }";

    mockMvc
        .perform(put(API).contentType(MediaType.APPLICATION_JSON).content(json2))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(API))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.value").value(""));
  }
}
