package bio.terra.user.app.controller;

import bio.terra.common.iam.SamUser;
import bio.terra.common.iam.SamUserFactory;
import bio.terra.user.app.configuration.SamConfiguration;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileSamUserFactory {

  private final SamUserFactory userFactory;
  private final SamConfiguration samConfiguration;

  @Autowired
  public ProfileSamUserFactory(SamUserFactory userFactory, SamConfiguration samConfiguration) {
    this.userFactory = userFactory;
    this.samConfiguration = samConfiguration;
  }

  public SamUser from(HttpServletRequest request) {
    return userFactory.from(request, samConfiguration.basePath());
  }
}
