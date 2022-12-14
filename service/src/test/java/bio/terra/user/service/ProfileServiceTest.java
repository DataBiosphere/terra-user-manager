package bio.terra.user.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.common.iam.SamUser;
import bio.terra.user.db.exception.BadPathException;
import bio.terra.user.service.exception.InvalidPropertyException;
import bio.terra.user.service.user.ProfileService;
import bio.terra.user.testutils.BaseUnitTest;
import bio.terra.user.testutils.TestUtils;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileServiceTest extends BaseUnitTest {

  @Autowired private ProfileService profileService;

  private final SamUser user = mock(SamUser.class);

  @BeforeEach
  void beforeEach() {
    when(user.getSubjectId()).thenReturn(TestUtils.appendRandomNumber("fake"));
  }

  @Test
  void setRoot() throws Exception {
    assertThrows(
        InvalidPropertyException.class,
        () -> profileService.setProperty(user.getSubjectId(), List.of(), null));
  }

  @Test
  void setProperty_rejectsLongPath() throws Exception {
    assertThrows(
        BadPathException.class,
        () -> profileService.setProperty(user.getSubjectId(), Collections.nCopies(33, "a"), null));
  }
}
