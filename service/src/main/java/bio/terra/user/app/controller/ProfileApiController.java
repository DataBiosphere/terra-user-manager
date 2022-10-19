package bio.terra.user.app.controller;

import bio.terra.common.iam.SamUser;
import bio.terra.user.api.ProfileApi;
import bio.terra.user.model.AnyObject;
import bio.terra.user.service.user.ProfileService;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileApiController implements ProfileApi {

  private final HttpServletRequest servletRequest;
  private final ProfileService profileService;
  private final ProfileSamUserFactory userFactory;

  @Autowired
  public ProfileApiController(
      HttpServletRequest servletRequest,
      ProfileService profileService,
      ProfileSamUserFactory userFactory) {
    this.servletRequest = servletRequest;
    this.profileService = profileService;
    this.userFactory = userFactory;
  }

  @Override
  public ResponseEntity<AnyObject> setUserProfile(AnyObject body, String path) {
    profileService.setProperty(getUser(), parsePath(path), body.getValue());

    return getUserProfile(path);
  }

  @Override
  public ResponseEntity<AnyObject> getUserProfile(String path) {
    AnyObject apiObj =
        new AnyObject().value(profileService.getProperty(getUser(), parsePath(path)));

    return new ResponseEntity<>(apiObj, HttpStatus.OK);
  }

  private SamUser getUser() {
    return userFactory.from(servletRequest);
  }

  private List<String> parsePath(String path) {
    if (StringUtils.isEmpty(path)) return List.of();

    return Arrays.asList(path.split("\\."));
  }
}
