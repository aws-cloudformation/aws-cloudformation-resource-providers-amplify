package software.amazon.amplify.branch;

import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.AutoBranchCreationConfig;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.awssdk.services.amplify.model.CreateBranchRequest;
import software.amazon.awssdk.services.amplify.model.CustomRule;
import software.amazon.awssdk.services.amplify.model.DeleteAppRequest;
import software.amazon.awssdk.services.amplify.model.DeleteBranchRequest;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
import software.amazon.awssdk.services.amplify.model.GetBranchRequest;
import software.amazon.awssdk.services.amplify.model.GetBranchResponse;
import software.amazon.awssdk.services.amplify.model.ListAppsRequest;
import software.amazon.awssdk.services.amplify.model.ListBranchesRequest;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.amplify.model.UpdateAppRequest;
import software.amazon.awssdk.services.amplify.model.UpdateBranchRequest;

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

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
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
   * @return awsRequest the aws service request to describe a resource
   */
  static GetBranchRequest translateToReadRequest(final ResourceModel model) {
    return GetBranchRequest.builder()
            .appId(model.getAppId())
            .branchName(model.getBranchName())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetBranchResponse response) {
    Branch branch = response.branch();

    ResourceModel.ResourceModelBuilder branchModelBuilder = ResourceModel.builder()
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
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteBranchRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteBranchRequest.builder()
            .appId(model.getAppId())
            .branchName(model.getBranchName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static UpdateBranchRequest translateToUpdateRequest(final ResourceModel model) {
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
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListBranchesRequest translateToListRequest(final ResourceModel model, String nextToken) {
    return ListBranchesRequest.builder()
            .appId(model.getAppId())
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final AwsResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
    return streamOfOrEmpty(Lists.newArrayList())
        .map(resource -> ResourceModel.builder()
            // include only primary identifier
            .build())
        .collect(Collectors.toList());
  }

  static ListTagsForResourceRequest translateToListTagsForResourceRequest(final String arn) {
    return ListTagsForResourceRequest.builder()
            .resourceArn(arn)
            .build();
  }

  /*
   * Helpers
   */
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
