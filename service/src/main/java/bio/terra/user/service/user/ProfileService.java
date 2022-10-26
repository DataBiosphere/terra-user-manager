package bio.terra.user.service.user;

import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.user.db.ProfileDao;
import bio.terra.user.service.exception.InvalidPropertyException;
import bio.terra.user.service.exception.MalformedPropertyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
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

  public void setProperty(String userId, List<String> path, Object value) {
    if (path.size() == 0) {
      throw new InvalidPropertyException("Cannot overwrite the root object.");
    }

    String json;
    try {
      json = objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new MalformedPropertyException(
          String.format("Value to set must be valid JSON: %s", value), e);
    }
    profileDao.setProperty(userId, path, json);
  }

  public Object getProperty(String userId, List<String> path) {
    var prop = profileDao.getProperty(userId, path);
    if (prop == null) return NullNode.getInstance();

    try {
      return objectMapper.readValue(prop, JsonNode.class);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException(
          String.format("Failed to process property json: %s", prop), e);
    }
  }
}
