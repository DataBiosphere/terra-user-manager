package bio.terra.user.db;

import bio.terra.common.db.ReadTransaction;
import bio.terra.common.db.WriteTransaction;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProfileDao {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public ProfileDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @WriteTransaction
  public void setProperty(String userId, List<String> path, String value) {
    createRowIfNotExists(userId);

    for (int i = 0; i < path.size() - 1; i++) {
      var subPath = path.subList(0, i + 1);

      if (!pathExists(userId, subPath)) {
        setPropertySingle(userId, subPath, "{}");
      }
    }

    setPropertySingle(userId, path, value);
  }

  private boolean pathExists(String userId, List<String> path) {
    return getPropertySingle(userId, path) != null;
  }

  private void setPropertySingle(String userId, List<String> path, String value) {
    final String sql =
        "UPDATE user_profile "
            + "SET profile_obj = jsonb_set(profile_obj, :path::text[], :value::jsonb) "
            + "WHERE user_id = :user_id";

    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("path", path == null ? new String[0] : path.toArray(String[]::new))
            .addValue("value", value)
            .addValue("user_id", userId);

    jdbcTemplate.update(sql, params);
  }

  @ReadTransaction
  public String getProperty(String userId, List<String> path) {
    return getPropertySingle(userId, path);
  }

  private String getPropertySingle(String userId, List<String> path) {
    final String sql =
        "SELECT profile_obj #> :path::text[] AS value "
            + "FROM user_profile "
            + "WHERE user_id = :user_id ";

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
      return "{}";
    }
  }

  private void createRowIfNotExists(String userId) {
    final String sql =
        "INSERT INTO user_profile (user_id) " + "VALUES (:user_id) " + "ON CONFLICT DO NOTHING";

    MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", userId);

    jdbcTemplate.update(sql, params);
  }
}
