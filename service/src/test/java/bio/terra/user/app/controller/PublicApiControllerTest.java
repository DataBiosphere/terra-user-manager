package bio.terra.user.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.user.app.configuration.VersionConfiguration;
import bio.terra.user.testutils.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class PublicApiControllerTest extends BaseTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private VersionConfiguration versionConfiguration;

  @Test
  void testStatus() throws Exception {
    this.mockMvc.perform(get("/status")).andExpect(status().isOk());
  }

  @Test
  void testVersion() throws Exception {
    this.mockMvc
        .perform(get("/version"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gitTag").value(versionConfiguration.getGitTag()))
        .andExpect(jsonPath("$.gitHash").value(versionConfiguration.getGitHash()))
        .andExpect(jsonPath("$.github").value(versionConfiguration.getGithub()))
        .andExpect(jsonPath("$.build").value(versionConfiguration.getBuild()));
  }

  @Test
  void testGetSwagger() throws Exception {
    this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
  }

  @Test
  void testIndex() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(redirectedUrl("swagger-ui.html"));
  }
}
