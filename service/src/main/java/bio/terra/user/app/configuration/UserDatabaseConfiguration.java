package bio.terra.user.app.configuration;

import java.util.Properties;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ConfigurationProperties(prefix = "user.user-database")
public class UserDatabaseConfiguration {
  // These properties control code in the StartupInitializer. We would not use these in production,
  // but they are handy to set for development and testing. There are only three interesting states:
  // 1. initialize is true; upgrade is irrelevant - initialize and recreate an empty database
  // 2. initialize is false; upgrade is true - apply changesets to an existing database
  // 3. initialize is false; upgrade is false - do nothing to the database
  /** If true, primary database will be wiped */
  private boolean initializeOnStart;
  /** If true, primary database will have changesets applied */
  private boolean upgradeOnStart;

  private String uri;
  private String username;
  private String password;

  private PoolingDataSource<PoolableConnection> dataSource;

  public String getUri() {
    return uri;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  // NOTE: even though the setters appear unused, the Spring infrastructure uses them to populate
  // the properties.
  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  // Main use of the configuration is this pooling data source object.
  public PoolingDataSource<PoolableConnection> getDataSource() {
    // Lazy allocation of the data source
    if (dataSource == null) {
      configureDataSource();
    }
    return dataSource;
  }

  private void configureDataSource() {
    Properties props = new Properties();
    props.setProperty("user", getUsername());
    props.setProperty("password", getPassword());

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(getUri(), props);

    PoolableConnectionFactory poolableConnectionFactory =
        new PoolableConnectionFactory(connectionFactory, null);

    ObjectPool<PoolableConnection> connectionPool =
        new GenericObjectPool<>(poolableConnectionFactory);

    poolableConnectionFactory.setPool(connectionPool);

    dataSource = new PoolingDataSource<>(connectionPool);
  }

  public boolean isInitializeOnStart() {
    return initializeOnStart;
  }

  public void setInitializeOnStart(boolean initializeOnStart) {
    this.initializeOnStart = initializeOnStart;
  }

  public boolean isUpgradeOnStart() {
    return upgradeOnStart;
  }

  public void setUpgradeOnStart(boolean upgradeOnStart) {
    this.upgradeOnStart = upgradeOnStart;
  }

  // This bean plus the @EnableTransactionManagement annotation above enables the use of the
  // @Transaction annotation to control the transaction properties of the data source.
  @Bean("transactionManager")
  public PlatformTransactionManager getTransactionManager() {
    return new JdbcTransactionManager(getDataSource());
  }
}
