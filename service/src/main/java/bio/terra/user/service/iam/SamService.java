package bio.terra.user.service.iam;

import bio.terra.common.iam.BearerToken;
import bio.terra.common.sam.SamRetry;
import bio.terra.common.sam.exception.SamExceptionFactory;
import bio.terra.user.app.configuration.SamConfiguration;
import org.broadinstitute.dsde.workbench.client.sam.ApiClient;
import org.broadinstitute.dsde.workbench.client.sam.ApiException;
import org.broadinstitute.dsde.workbench.client.sam.api.AdminApi;
import org.broadinstitute.dsde.workbench.client.sam.api.UsersApi;
import org.broadinstitute.dsde.workbench.client.sam.model.UserStatusInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SamService {
  private final SamConfiguration samConfig;

  @Autowired
  public SamService(SamConfiguration samConfig) {
    this.samConfig = samConfig;
  }

  private ApiClient getApiClient(String accessToken) {
    ApiClient apiClient = getApiClient();
    apiClient.setAccessToken(accessToken);
    return apiClient;
  }

  private ApiClient getApiClient() {
    return new ApiClient().setBasePath(samConfig.basePath());
  }

  public UsersApi samUsersApi(String accessToken) {
    return new UsersApi(getApiClient(accessToken));
  }

  public AdminApi samAdminApi(String accessToken) {
    return new AdminApi(getApiClient(accessToken));
  }

  public UserStatusInfo getUserStatusInfo(BearerToken userRequest) throws InterruptedException {
    UsersApi usersApi = samUsersApi(userRequest.getToken());
    try {
      return SamRetry.retry(usersApi::getUserStatusInfo);
    } catch (ApiException apiException) {
      throw SamExceptionFactory.create("Error getting user status info from Sam", apiException);
    }
  }

  public String getUserEmailFromSam(BearerToken userRequest) throws InterruptedException {
    return getUserStatusInfo(userRequest).getUserEmail();
  }

  public String adminGetUserIdByEmail(BearerToken userRequest, String email)
      throws InterruptedException {
    try {
      return SamRetry.retry(
          () ->
              samAdminApi(userRequest.getToken())
                  .adminGetUserByEmail(email)
                  .getUserInfo()
                  .getUserSubjectId());
    } catch (ApiException apiException) {
      throw SamExceptionFactory.create(
          String.format("Failed to get user id for email %s", email), apiException);
    }
  }
}
