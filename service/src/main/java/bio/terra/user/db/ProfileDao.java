package bio.terra.user.db;

import bio.terra.common.db.ReadTransaction;
import bio.terra.common.db.WriteTransaction;
import bio.terra.user.db.exception.BadPathException;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ProfileDao {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public ProfileDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * When attempting to set a nested property, the service will ensure the path to that property
   * exists and is traversable.
   *
   * <p>a) If any path elements are not found, then it will initialize those elements to an empty
   * object before continuing.
   *
   * <p>b) If a path element exists but is not an indexible type (object or array), then the
   * operation will abort.
   *
   * @param userId - Sam user ID
   * @param path - path to potentially nested property
   * @param value - JSON serialized value
   */
  @WriteTransaction
  public void setProperty(String userId, List<String> path, String value) {
    createRowIfNotExists(userId);

    final String sql =
        """
        UPDATE user_profile
        SET profile_obj = jsonb_strip_nulls(pathRecurse(profile_obj, :path::text[], :value::jsonb))
        WHERE user_id = :user_id
        """;

    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("path", path == null ? new String[0] : path.toArray(String[]::new))
            .addValue("value", value)
            .addValue("user_id", userId);

    try {
      jdbcTemplate.update(sql, params);
    } catch (DataIntegrityViolationException e) {
      throw new BadPathException(
          "Access through the path " + path + " has failed: " + e.getCause().getMessage());
    }
  }

  @ReadTransaction
  @Nullable
  public String getProperty(String userId, List<String> path) {
    final String sql =
        """
        SELECT profile_obj #> :path::text[] AS value
        FROM user_profile
        WHERE user_id = :user_id
        """;

    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("path", path == null ? new String[0] : path.toArray(String[]::new))
            .addValue("user_id", userId);

    try {
      return jdbcTemplate.queryForObject(
          sql,
          params,
          (rs, rowNum) -> {
            return rs.getString("value");
          });
    } catch (EmptyResultDataAccessException e) {
      // If the row for the user doesn't exist yet, we try and
      // keep the behaviour consistent to the client by pretending
      // the profile object is empty and/or the specific path requested
      // does not exist.
      return CollectionUtils.isEmpty(path) ? "{}" : null;
    }
  }

  private void createRowIfNotExists(String userId) {
    final String sql =
        """
        INSERT INTO user_profile (user_id)
        VALUES (:user_id)
        ON CONFLICT DO NOTHING
        """;

    MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", userId);

    jdbcTemplate.update(sql, params);
  }
}
