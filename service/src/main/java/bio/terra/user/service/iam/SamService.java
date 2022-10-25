package bio.terra.user.service.iam;

import bio.terra.common.exception.ForbiddenException;
import bio.terra.common.iam.BearerToken;
import bio.terra.common.sam.SamRetry;
import bio.terra.user.app.configuration.SamConfiguration;
import org.broadinstitute.dsde.workbench.client.sam.ApiClient;
import org.broadinstitute.dsde.workbench.client.sam.ApiException;
import org.broadinstitute.dsde.workbench.client.sam.api.AdminApi;
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

  public AdminApi samAdminApi(String accessToken) {
    return new AdminApi(getApiClient(accessToken));
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
      throw new ForbiddenException("Terra Admin access is required for this action.");
    }
  }
}
