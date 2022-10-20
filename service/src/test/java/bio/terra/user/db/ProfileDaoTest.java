package bio.terra.user.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.user.db.exception.BadPathException;
import bio.terra.user.testutils.BaseUnitTest;
import bio.terra.user.testutils.TestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileDaoTest extends BaseUnitTest {
  @Autowired private ProfileDao profileDao;

  @Test
  void getEmptyProfile() throws Exception {
    var userId = TestUtils.appendRandomNumber("fake");
    assertEquals(profileDao.getProperty(userId, List.of()), "{}");
  }

  @Test
  void recursiveSet() throws Exception {
    var userId = TestUtils.appendRandomNumber("fake");
    var path = List.of("user", "name", "first");
    var name = "\"John\"";
    profileDao.setProperty(userId, path, name);
    assertEquals(name, profileDao.getProperty(userId, path));
  }

  @Test
  void setSideBySide() throws Exception {
    var userId = TestUtils.appendRandomNumber("fake");
    var darkmode = List.of("settings", "darkmode");
    var language = List.of("settings", "language");
    profileDao.setProperty(userId, darkmode, "true");
    profileDao.setProperty(userId, language, "\"en\"");

    assertEquals("true", profileDao.getProperty(userId, darkmode));
    assertEquals("\"en\"", profileDao.getProperty(userId, language));
  }

  @Test
  void clobberSet() throws Exception {
    var userId = TestUtils.appendRandomNumber("fake");
    var path = List.of("starred");
    var workspaces = "[\"workspace1\", \"workspace2\"]";
    profileDao.setProperty(userId, path, workspaces);
    assertEquals(workspaces, profileDao.getProperty(userId, path));

    profileDao.setProperty(userId, path, "null");
    assertEquals(null, profileDao.getProperty(userId, path));
  }

  @Test
  void badPath() throws Exception {
    var userId = TestUtils.appendRandomNumber("fake");
    profileDao.setProperty(userId, List.of("a"), "\"b\"");
    assertEquals("\"b\"", profileDao.getProperty(userId, List.of("a")));

    assertThrows(
        BadPathException.class,
        () -> profileDao.setProperty(userId, List.of("a", "prop"), "\"c\""));
  }
}
