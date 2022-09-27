package bio.terra.user.app;

import bio.terra.common.migrate.LiquibaseMigrator;
import bio.terra.user.app.configuration.UserDatabaseConfiguration;
import org.springframework.context.ApplicationContext;

public final class StartupInitializer {
  private static final String changelogPath = "db/changelog.xml";

  public static void initialize(ApplicationContext applicationContext) {
    // Initialize or upgrade the database depending on the configuration
    LiquibaseMigrator migrateService = applicationContext.getBean(LiquibaseMigrator.class);
    var databaseConfiguration = applicationContext.getBean(UserDatabaseConfiguration.class);

    // Migrate the database
    if (databaseConfiguration.isInitializeOnStart()) {
      migrateService.initialize(changelogPath, databaseConfiguration.getDataSource());
    } else if (databaseConfiguration.isUpgradeOnStart()) {
      migrateService.upgrade(changelogPath, databaseConfiguration.getDataSource());
    }
  }
}
