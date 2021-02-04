package software.amazon.amplify.branch;

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import software.amazon.amplify.common.utils.ArnUtils;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.awssdk.services.amplify.model.CreateBranchRequest;
import software.amazon.awssdk.services.amplify.model.DeleteBranchRequest;
import software.amazon.awssdk.services.amplify.model.GetBranchRequest;
import software.amazon.awssdk.services.amplify.model.GetBranchResponse;
import software.amazon.awssdk.services.amplify.model.ListBranchesRequest;
import software.amazon.awssdk.services.amplify.model.ListBranchesResponse;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.amplify.model.UpdateBranchRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {
  private static final String ARN_SPLIT_KEY = "/branches/";

  /**
   * Request to create a resource
   * @param model resource model
   * @return createBranchRequest the aws service request to create a resource
   */
  static CreateBranchRequest translateToCreateRequest(final ResourceModel model) {
    final CreateBranchRequest.Builder createBranchRequest = CreateBranchRequest.builder()
            .appId(model.getAppId())
            .backendEnvironmentArn(model.getBackendEnvironmentArn())
            .branchName(model.getBranchName())
            .buildSpec(model.getBuildSpec())
            .description(model.getDescription())
            .displayName(model.getDisplayName())
            .enableAutoBuild(model.getEnableAutoBuild())
            .enableNotification(model.getEnableNotification())
            .enablePerformanceMode(model.getEnablePerformanceMode())
            .enablePullRequestPreview(model.getEnablePullRequestPreview())
            .framework(model.getFramework())
            .pullRequestEnvironmentName(model.getPullRequestEnvironmentName())
            .stage(model.getStage())
            .ttl(model.getTtl());

    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      createBranchRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      createBranchRequest.basicAuthCredentials(getBasicAuthCredentials(basicAuthConfig));
    }

    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      createBranchRequest.environmentVariables(getEnvironmentVariables(environmentVariables));
    }

    List<Tag> appTags = model.getTags();
    if (CollectionUtils.isNotEmpty(appTags)) {
      createBranchRequest.tags(getTags(appTags));
    }
    return createBranchRequest.build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return getBranchRequest the aws service request to describe a resource
   */
  static GetBranchRequest translateToReadRequest(final ResourceModel model) {
    initializeModel(model);
    return GetBranchRequest.builder()
            .appId(model.getAppId())
            .branchName(model.getBranchName())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param getBranchResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetBranchResponse getBranchResponse) {
    final Branch branch = getBranchResponse.branch();

    ResourceModel.ResourceModelBuilder branchModelBuilder = ResourceModel.builder()
            .appId(ArnUtils.getAppId(branch.branchArn(), ARN_SPLIT_KEY))
            .arn(branch.branchArn())
            .branchName(branch.branchName())
            .buildSpec(branch.buildSpec())
            .description(branch.description())
            .displayName(branch.displayName())
            .enableAutoBuild(branch.enableAutoBuild())
            .enableNotification(branch.enableNotification())
            .enablePerformanceMode(branch.enablePerformanceMode())
            .enablePullRequestPreview(branch.enablePullRequestPreview())
            .framework(branch.framework())
            .pullRequestEnvironmentName(branch.pullRequestEnvironmentName())
            .stage(branch.stageAsString())
            .ttl(branch.ttl());

    Map<String, String> branchEnvVars = branch.environmentVariables();
    if (MapUtils.isNotEmpty(branchEnvVars)) {
      branchModelBuilder.environmentVariables(getEnvironmentVariablesCFN(branchEnvVars));
    }

    Map<String, String> branchTags = branch.tags();
    if (MapUtils.isNotEmpty(branchTags)) {
      branchModelBuilder.tags(getTagsCFN(branchTags));
    }
    return branchModelBuilder.build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return deleteBranchRequest the aws service request to delete a resource
   */
  static DeleteBranchRequest translateToDeleteRequest(final ResourceModel model) {
    initializeModel(model);
    return DeleteBranchRequest.builder()
            .appId(model.getAppId())
            .branchName(model.getBranchName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return updateBranchRequest the aws service request to modify a resource
   */
  static UpdateBranchRequest translateToUpdateRequest(final ResourceModel model) {
    initializeModel(model);
    final UpdateBranchRequest.Builder updateBranchRequest = UpdateBranchRequest.builder()
            .appId(model.getAppId())
            .branchName(model.getBranchName())
            .backendEnvironmentArn(model.getBackendEnvironmentArn())
            .buildSpec(model.getBuildSpec())
            .description(model.getDescription())
            .displayName(model.getDisplayName())
            .enableAutoBuild(model.getEnableAutoBuild())
            .enableNotification(model.getEnableNotification())
            .enablePerformanceMode(model.getEnablePerformanceMode())
            .enablePullRequestPreview(model.getEnablePullRequestPreview())
            .framework(model.getFramework())
            .pullRequestEnvironmentName(model.getPullRequestEnvironmentName())
            .stage(model.getStage())
            .ttl(model.getTtl());

    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      updateBranchRequest.environmentVariables(getEnvironmentVariables(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      updateBranchRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      updateBranchRequest.basicAuthCredentials(getBasicAuthCredentials(basicAuthConfig));
    }
    return updateBranchRequest.build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return listBranchesRequest the aws service request to list resources within aws account
   */
  static ListBranchesRequest translateToListRequest(final ResourceModel model, String nextToken) {
    return ListBranchesRequest.builder()
            .appId(model.getAppId())
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param listBranchesResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListBranchesResponse listBranchesResponse) {
    return streamOfOrEmpty(listBranchesResponse.branches())
        .map(resource -> ResourceModel.builder()
            .arn(resource.branchArn())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Request to list tags
   * @param arn string
   * @return listTagsForResourceRequest
   */
  static ListTagsForResourceRequest translateToListTagsForResourceRequest(final String arn) {
    return ListTagsForResourceRequest.builder()
            .resourceArn(arn)
            .build();
  }

  /*
   * Helpers
   */
  private static void initializeModel(@NonNull ResourceModel model) {
    if (model.getAppId() == null || model.getBranchName() == null) {
      String arn = model.getArn();
      if (arn == null) {
        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
      }
      try {
        model.setAppId(ArnUtils.getAppId(arn, ARN_SPLIT_KEY));
        model.setBranchName(ArnUtils.getResourceName(arn, ARN_SPLIT_KEY));
      } catch (Exception e) {
        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
      }
    }
  }

  private static List<EnvironmentVariable> getEnvironmentVariablesCFN(@NonNull final Map<String, String> envVars) {
    List<EnvironmentVariable> envVarsCFN = new ArrayList<>();
    envVars.forEach((k, v) -> envVarsCFN.add(EnvironmentVariable.builder()
            .name(k)
            .value(v)
            .build()));
    return envVarsCFN;
  }

  private static List<Tag> getTagsCFN(@NonNull final Map<String, String> tags) {
    List<Tag> tagsCFN = new ArrayList<>();
    tags.forEach((k, v) -> tagsCFN.add(Tag.builder()
            .key(k)
            .value(v)
            .build()));
    return tagsCFN;
  }

  public static Map<String, String> getTags(@NonNull final List<Tag> tags) {
    Map<String, String> tagMap = new HashMap<>();
    for (Tag tag : tags) {
      tagMap.put(tag.getKey(), tag.getValue());
    }
    return tagMap;
  }

  private static Map<String, String> getEnvironmentVariables(@NonNull final List<EnvironmentVariable> envVarsCFN) {
    Map<String, String> envVars = new HashMap<>();
    for (EnvironmentVariable envVarCFN : envVarsCFN) {
      envVars.put(envVarCFN.getName(), envVarCFN.getValue());
    }
    return envVars;
  }

  private static String getBasicAuthCredentials(@NonNull BasicAuthConfig basicAuthConfig) {
    final String userInfo = String.format("%s:%s", basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
    return Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
