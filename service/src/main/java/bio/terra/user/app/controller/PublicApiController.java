package bio.terra.user.app.controller;

import bio.terra.user.api.PublicApi;
import bio.terra.user.app.configuration.VersionConfiguration;
import bio.terra.user.model.VersionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

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
  public ResponseEntity<VersionProperties> serviceVersion() {
    VersionProperties currentVersion =
        new VersionProperties()
            .gitTag(versionConfiguration.getGitTag())
            .gitHash(versionConfiguration.getGitHash())
            .github(versionConfiguration.getGithub())
            .build(versionConfiguration.getBuild());
    return new ResponseEntity<>(currentVersion, HttpStatus.OK);
  }
}
