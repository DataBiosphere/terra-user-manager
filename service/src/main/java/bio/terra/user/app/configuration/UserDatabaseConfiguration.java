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
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@ConfigurationProperties(prefix = "user.user-database")
public record UserDatabaseConfiguration(
    boolean initializeOnStart,
    boolean upgradeOnStart,
    String uri,
    String username,
    String password,
    PoolingDataSource<PoolableConnection> dataSource) {

  public UserDatabaseConfiguration {
    Properties props = new Properties();
    props.setProperty("user", username);
    props.setProperty("password", password);

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, props);

    PoolableConnectionFactory poolableConnectionFactory =
        new PoolableConnectionFactory(connectionFactory, null);

    ObjectPool<PoolableConnection> connectionPool =
        new GenericObjectPool<>(poolableConnectionFactory);

    poolableConnectionFactory.setPool(connectionPool);

    dataSource = new PoolingDataSource<>(connectionPool);
  }

  @Bean("transactionManager")
  public PlatformTransactionManager getTransactionManager() {
    return new JdbcTransactionManager(dataSource());
  }
}
