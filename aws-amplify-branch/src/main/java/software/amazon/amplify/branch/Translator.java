package software.amazon.amplify.branch;

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
            .branchName(model.getBranchName())
            .buildSpec(model.getBuildSpec())
            .description(model.getDescription())
            .enableAutoBuild(model.getEnableAutoBuild())
            .enablePerformanceMode(model.getEnablePerformanceMode())
            .enablePullRequestPreview(model.getEnablePullRequestPreview())
            .pullRequestEnvironmentName(model.getPullRequestEnvironmentName())
            .stage(model.getStage());

    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      createBranchRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      createBranchRequest.basicAuthCredentials(getBasicAuthCredentialsSDK(basicAuthConfig));
    }

    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      createBranchRequest.environmentVariables(getEnvironmentVariablesSDK(environmentVariables));
    }

    List<Tag> appTags = model.getTags();
    if (CollectionUtils.isNotEmpty(appTags)) {
      createBranchRequest.tags(getTagsSDK(appTags));
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
            .enableAutoBuild(branch.enableAutoBuild())
            .enablePerformanceMode(branch.enablePerformanceMode())
            .enablePullRequestPreview(branch.enablePullRequestPreview())
            .pullRequestEnvironmentName(branch.pullRequestEnvironmentName())
            .stage(branch.stageAsString());

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
            .buildSpec(model.getBuildSpec())
            .description(model.getDescription())
            .enableAutoBuild(model.getEnableAutoBuild())
            .enablePerformanceMode(model.getEnablePerformanceMode())
            .enablePullRequestPreview(model.getEnablePullRequestPreview())
            .pullRequestEnvironmentName(model.getPullRequestEnvironmentName())
            .stage(model.getStage());

    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (environmentVariables != null) {
      updateBranchRequest.environmentVariables(getEnvironmentVariablesSDK(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      updateBranchRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      updateBranchRequest.basicAuthCredentials(getBasicAuthCredentialsSDK(basicAuthConfig));
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
  private static void initializeModel(ResourceModel model) {
    if (model.getAppId() == null || model.getBranchName() == null) {
      String arn = model.getArn();
      if (arn == null) {
        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
      }
      model.setAppId(ArnUtils.getAppId(arn, ARN_SPLIT_KEY));
      model.setBranchName(ArnUtils.getResourceName(arn, ARN_SPLIT_KEY));
    }
  }

  static List<EnvironmentVariable> getEnvironmentVariablesCFN(final Map<String, String> envVars) {
    List<EnvironmentVariable> envVarsCFN = new ArrayList<>();
    envVars.forEach((k, v) -> envVarsCFN.add(EnvironmentVariable.builder()
            .name(k)
            .value(v)
            .build()));
    return envVarsCFN;
  }

  static List<Tag> getTagsCFN(final Map<String, String> tags) {
    List<Tag> tagsCFN = new ArrayList<>();
    tags.forEach((k, v) -> tagsCFN.add(Tag.builder()
            .key(k)
            .value(v)
            .build()));
    return tagsCFN;
  }

  static Map<String, String> getTagsSDK(final List<Tag> tags) {
    Map<String, String> tagMap = new HashMap<>();
    for (Tag tag : tags) {
      tagMap.put(tag.getKey(), tag.getValue());
    }
    return tagMap;
  }

  static Map<String, String> getEnvironmentVariablesSDK(final List<EnvironmentVariable> envVarsCFN) {
    Map<String, String> envVars = new HashMap<>();
    for (EnvironmentVariable envVarCFN : envVarsCFN) {
      envVars.put(envVarCFN.getName(), envVarCFN.getValue());
    }
    return envVars;
  }

  static String getBasicAuthCredentialsSDK(BasicAuthConfig basicAuthConfig) {
    final String userInfo = String.format("%s:%s", basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
    return Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
  }

  static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
