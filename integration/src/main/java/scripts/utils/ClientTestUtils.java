package scripts.utils;

import bio.terra.testrunner.common.utils.AuthenticationUtils;
import bio.terra.testrunner.runner.config.ServerSpecification;
import bio.terra.testrunner.runner.config.TestUserSpecification;
import bio.terra.user.client.ApiClient;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTestUtils {
  private static final Logger logger = LoggerFactory.getLogger(ClientTestUtils.class);

  // Required scopes for client tests include the usual login scopes and GCP scope.
  public static final List<String> TEST_USER_SCOPES =
      List.of("openid", "email", "profile", "https://www.googleapis.com/auth/cloud-platform");

  private ClientTestUtils() {}

  public static ApiClient getClientForTestUser(
      TestUserSpecification testUser, ServerSpecification server) throws IOException {
    AccessToken accessToken = null;

    if (testUser != null) {
      logger.debug(
          "Fetching credentials and building Workspace Manager ApiClient object for test user: {}",
          testUser.name);
      GoogleCredentials userCredential =
          AuthenticationUtils.getDelegatedUserCredential(testUser, TEST_USER_SCOPES);
      accessToken = AuthenticationUtils.getAccessToken(userCredential);
    }

    return buildClient(accessToken, server);
  }

  public static ApiClient getClientWithoutAccessToken(ServerSpecification server)
      throws IOException {
    return buildClient(null, server);
  }

  // TODO: Need to add userServiceUri to the ServerSpecification in TestRunner.
  //  Although that seems crazy to have to update TestRunner for every component.
  private static ApiClient buildClient(
      @Nullable AccessToken accessToken, ServerSpecification server) throws IOException {
    if (Strings.isNullOrEmpty(server.workspaceManagerUri)) {
      throw new IllegalArgumentException("User Service URI cannot be empty");
    }

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(server.workspaceManagerUri);

    if (accessToken != null) {
      apiClient.setAccessToken(accessToken.getTokenValue());
    }

    return apiClient;
  }
}
