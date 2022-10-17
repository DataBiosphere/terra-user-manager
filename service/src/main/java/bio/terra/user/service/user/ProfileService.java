package bio.terra.user.service.user;

import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.common.iam.SamUser;
import bio.terra.user.db.ProfileDao;
import bio.terra.user.service.exception.InvalidPropertyException;
import bio.terra.user.service.exception.MalformedPropertyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileService {

  private final ProfileDao profileDao;
  private final ObjectMapper objectMapper;

  @Autowired
  public ProfileService(ProfileDao profileDao, ObjectMapper objectMapper) {
    this.profileDao = profileDao;
    this.objectMapper = objectMapper;
  }

  public void setProperty(SamUser user, List<String> path, Object value) {
    if (path.size() == 0) {
      throw new InvalidPropertyException("Cannot overwrite the root object.");
    }

    var userId = user.getSubjectId();
    try {
      var json = objectMapper.writeValueAsString(value);

      profileDao.setProperty(userId, path, json);
    } catch (JsonProcessingException e) {
      throw new MalformedPropertyException("Value to set must be valid JSON", e);
    }
  }

  public Object getProperty(SamUser user, List<String> path) {
    var userId = user.getSubjectId();

    var prop = profileDao.getProperty(userId, path);
    if (prop == null) return null;

    try {
      return objectMapper.readValue(prop, JsonNode.class);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Failed to process property json", e);
    }
  }
}
