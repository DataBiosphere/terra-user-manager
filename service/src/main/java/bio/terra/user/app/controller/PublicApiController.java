package bio.terra.user.app.controller;

import bio.terra.user.api.PublicApi;
import bio.terra.user.app.configuration.VersionConfiguration;
import bio.terra.user.model.ApiVersionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicApiController implements PublicApi {
  private final VersionConfiguration versionConfiguration;

  @Autowired
  public PublicApiController(VersionConfiguration versionConfiguration) {
    this.versionConfiguration = versionConfiguration;
  }

  @Override
  public ResponseEntity<Void> serviceStatus() {
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ApiVersionProperties> serviceVersion() {
    ApiVersionProperties currentVersion =
        new ApiVersionProperties()
            .gitTag(versionConfiguration.getGitTag())
            .gitHash(versionConfiguration.getGitHash())
            .github(versionConfiguration.getGithub())
            .build(versionConfiguration.getBuild());
    return new ResponseEntity<>(currentVersion, HttpStatus.OK);
  }

  @GetMapping(value = "/")
  public String index() {
    return "redirect:swagger-ui.html";
  }

  @GetMapping(value = "/swagger-ui.html")
  public String getSwagger(Model model) {
    return "index";
  }
}
