package software.amazon.amplify.app;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.amplify.common.utils.ArnUtils;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.CreateAppRequest;
import software.amazon.awssdk.services.amplify.model.CustomRule;
import software.amazon.awssdk.services.amplify.model.AutoBranchCreationConfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import software.amazon.awssdk.services.amplify.model.DeleteAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
import software.amazon.awssdk.services.amplify.model.ListAppsRequest;
import software.amazon.awssdk.services.amplify.model.ListAppsResponse;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.amplify.model.UpdateAppRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {
  private static final String ARN_SPLIT_KEY = " ";
  /**
   * Request to create a resource
   * @param model resource model
   * @return createAppRequest the aws service request to create a resource
   */
  static CreateAppRequest translateToCreateRequest(final ResourceModel model) {
    final CreateAppRequest.Builder createAppRequest = CreateAppRequest.builder()
            .name(model.getName())
            .description(model.getDescription())
            .oauthToken(model.getOauthToken())
            .repository(model.getRepository())
            .iamServiceRoleArn(model.getIAMServiceRole())
            .buildSpec(model.getBuildSpec())
            .accessToken(model.getAccessToken())
            .enableBranchAutoDeletion(model.getEnableBranchAutoDeletion())
            .customHeaders(model.getCustomHeaders());

    List<software.amazon.amplify.app.CustomRule> customRules = model.getCustomRules();
    if (CollectionUtils.isNotEmpty(customRules)) {
      createAppRequest.customRules(getCustomRulesSDK(customRules));
    }
    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      createAppRequest.environmentVariables(getEnvironmentVariablesSDK(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      createAppRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      createAppRequest.basicAuthCredentials(getBasicAuthCredentialsSDK(basicAuthConfig));
    }
    software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfigCFN = model.getAutoBranchCreationConfig();
    if (autoBranchCreationConfigCFN != null) {
      createAppRequest.enableAutoBranchCreation(autoBranchCreationConfigCFN.getEnableAutoBranchCreation());
      if (autoBranchCreationConfigCFN.getEnableAutoBranchCreation()) {
        createAppRequest.autoBranchCreationConfig(getAutoBranchCreationConfigSDK(autoBranchCreationConfigCFN));
        List<String> autoBranchCreationPatterns = autoBranchCreationConfigCFN.getAutoBranchCreationPatterns();
        if (CollectionUtils.isNotEmpty(autoBranchCreationPatterns)) {
          createAppRequest.autoBranchCreationPatterns(autoBranchCreationPatterns);
        }
      }
    }

    List<Tag> appTags = model.getTags();
    if (CollectionUtils.isNotEmpty(appTags)) {
      createAppRequest.tags(getTagsSDK(appTags));
    }
    return createAppRequest.build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return getAppRequest the aws service request to describe a resource
   */
  static GetAppRequest translateToReadRequest(final ResourceModel model) {
    initializeModel(model);
    return GetAppRequest.builder()
            .appId(model.getAppId())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param getAppResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetAppResponse getAppResponse) {
    ResourceModel.ResourceModelBuilder appModelBuilder = ResourceModel.builder();
    try {
      App app = getAppResponse.app();

      appModelBuilder
              .appId(app.appId())
              .appName(app.name())
              .arn(app.appArn())
              .buildSpec(app.buildSpec())
              .customHeaders(app.customHeaders())
              .description(app.description())
              .defaultDomain(app.defaultDomain())
              .enableBranchAutoDeletion(app.enableBranchAutoDeletion())
              .iAMServiceRole(app.iamServiceRoleArn())
              .repository(app.repository());

      Map<String, String> appEnvVars = app.environmentVariables();
      if (MapUtils.isNotEmpty(appEnvVars)) {
        appModelBuilder.environmentVariables(getEnvironmentVariablesCFN(appEnvVars));
      }

      if (CollectionUtils.isNotEmpty(app.customRules())) {
        List<software.amazon.amplify.app.CustomRule> customRulesCFN = new ArrayList<>();
        for (CustomRule customRule : app.customRules()) {
          customRulesCFN.add(software.amazon.amplify.app.CustomRule.builder()
                  .source(customRule.source())
                  .target(customRule.target())
                  .status(customRule.status())
                  .condition(customRule.condition())
                  .build());
        }
        appModelBuilder.customRules(customRulesCFN);
      }

      Map<String, String> appTags = app.tags();
      if (MapUtils.isNotEmpty(appTags)) {
        appModelBuilder.tags(getTagsCFN(appTags));
      }
      return appModelBuilder.build();
    } catch (final Exception e ) {
      System.out.println("*** [DEV] unhandled exception : " + e);
      e.printStackTrace();
    }
    return appModelBuilder.build();

  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return deleteAppRequest the aws service request to delete a resource
   */
  static DeleteAppRequest translateToDeleteRequest(final ResourceModel model) {
    initializeModel(model);
    return DeleteAppRequest.builder()
            .appId(model.getAppId())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return updateAppRequest the aws service request to modify a resource
   */
  static UpdateAppRequest translateToUpdateRequest(final ResourceModel model) {
    initializeModel(model);
    final UpdateAppRequest.Builder updateAppRequest = UpdateAppRequest.builder()
            .appId(model.getAppId())
            .name(model.getName())
            .description(model.getDescription())
            .oauthToken(model.getOauthToken())
            .repository(model.getRepository())
            .iamServiceRoleArn(model.getIAMServiceRole())
            .buildSpec(model.getBuildSpec())
            .accessToken(model.getAccessToken())
            .enableBranchAutoDeletion(model.getEnableBranchAutoDeletion())
            .customHeaders(model.getCustomHeaders());

    List<software.amazon.amplify.app.CustomRule> customRules = model.getCustomRules();
    if (CollectionUtils.isNotEmpty(customRules)) {
      updateAppRequest.customRules(getCustomRulesSDK(customRules));
    }
    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      updateAppRequest.environmentVariables(getEnvironmentVariablesSDK(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      updateAppRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      updateAppRequest.basicAuthCredentials(getBasicAuthCredentialsSDK(basicAuthConfig));
    }
    software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfig = model.getAutoBranchCreationConfig();
    if (autoBranchCreationConfig != null) {
      updateAppRequest.enableAutoBranchCreation(autoBranchCreationConfig.getEnableAutoBranchCreation());
      updateAppRequest.autoBranchCreationConfig(getAutoBranchCreationConfigSDK(autoBranchCreationConfig));
    }
    return updateAppRequest.build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return listAppsRequest the aws service request to list resources within aws account
   */
  static ListAppsRequest translateToListRequest(final String nextToken) {
    return ListAppsRequest.builder()
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param listAppsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListAppsResponse listAppsResponse) {
    return streamOfOrEmpty(listAppsResponse.apps())
        .map(resource -> ResourceModel.builder()
            .arn(resource.appArn())
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
  private static void initializeModel(final ResourceModel model) {
    if (model.getAppId() == null) {
      String arn = model.getArn();
      if (arn == null) {
        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
      }
      try {
        model.setAppId(ArnUtils.getAppId(arn, ARN_SPLIT_KEY));
      } catch (Exception e) {
        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
      }
    }
  }

  public static Map<String, String> getTagsSDK(@NonNull final List<Tag> tags) {
    Map<String, String> tagMap = new HashMap<>();
    for (Tag tag : tags) {
      tagMap.put(tag.getKey(), tag.getValue());
    }
    return tagMap;
  }

  @VisibleForTesting
  static List<Tag> getTagsCFN(@NonNull final Map<String, String> tags) {
    List<Tag> tagsCFN = new ArrayList<>();
    tags.forEach((k, v) -> tagsCFN.add(Tag.builder()
            .key(k)
            .value(v)
            .build()));
    return tagsCFN;
  }

  @VisibleForTesting
  static List<CustomRule> getCustomRulesSDK(@NonNull final List<software.amazon.amplify.app.CustomRule> customRulesCFN) {
    List<CustomRule> customRules = new ArrayList<>();
    for (software.amazon.amplify.app.CustomRule customRuleCFN: customRulesCFN) {
      final CustomRule customRule = CustomRule.builder()
              .source(customRuleCFN.getSource())
              .target(customRuleCFN.getTarget())
              .status(customRuleCFN.getStatus())
              .condition(customRuleCFN.getCondition())
              .build();
      customRules.add(customRule);
    }
    return customRules;
  }

  @VisibleForTesting
  static Map<String, String> getEnvironmentVariablesSDK(@NonNull final List<EnvironmentVariable> envVarsCFN) {
    Map<String, String> envVars = new HashMap<>();
    for (EnvironmentVariable envVarCFN : envVarsCFN) {
      envVars.put(envVarCFN.getName(), envVarCFN.getValue());
    }
    return envVars;
  }

  @VisibleForTesting
  static List<EnvironmentVariable> getEnvironmentVariablesCFN(@NonNull final Map<String, String> envVars) {
    List<EnvironmentVariable> envVarsCFN = new ArrayList<>();
    envVars.forEach((k, v) -> envVarsCFN.add(EnvironmentVariable.builder()
            .name(k)
            .value(v)
            .build()));
    return envVarsCFN;
  }

  @VisibleForTesting
  static String getBasicAuthCredentialsSDK(@NonNull BasicAuthConfig basicAuthConfig) {
    if (basicAuthConfig.getEnableBasicAuth() == null || basicAuthConfig.getEnableBasicAuth() == false) {
      return null;
    }
    if (StringUtils.isEmpty(basicAuthConfig.getUsername()) ||
        StringUtils.isEmpty(basicAuthConfig.getPassword())) {
      final String INVALID_PASSWORD = "Username or Password cannot be empty";
      throw new CfnInvalidRequestException(INVALID_PASSWORD);
    }
    final String userInfo = String.format("%s:%s", basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
    return Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
  }

  @VisibleForTesting
  static BasicAuthConfig getBasicAuthConfigCFN(@NonNull String basicAuthConfigString) {
    final String DELIMITER = ":";
    final String userInfo = Arrays.toString(Base64.getDecoder().decode(basicAuthConfigString));
    return BasicAuthConfig.builder()
            .username(userInfo.split(DELIMITER)[0])
            .password(userInfo.split(DELIMITER)[1])
            .enableBasicAuth(true)
            .build();
  }

  @VisibleForTesting
  static AutoBranchCreationConfig getAutoBranchCreationConfigSDK(@NonNull final software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfigCFN) {
    AutoBranchCreationConfig.Builder autoBranchCreationConfig = AutoBranchCreationConfig.builder()
            .buildSpec(autoBranchCreationConfigCFN.getBuildSpec())
            .stage(autoBranchCreationConfigCFN.getStage())
            .enableAutoBuild(autoBranchCreationConfigCFN.getEnableAutoBuild())
            .enablePullRequestPreview(autoBranchCreationConfigCFN.getEnablePullRequestPreview())
            .enablePerformanceMode(autoBranchCreationConfigCFN.getEnablePerformanceMode())
            .pullRequestEnvironmentName(autoBranchCreationConfigCFN.getPullRequestEnvironmentName());

    List<EnvironmentVariable> envVarsCFN = autoBranchCreationConfigCFN.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(envVarsCFN)) {
      autoBranchCreationConfig.environmentVariables(getEnvironmentVariablesSDK(envVarsCFN));
    }
    BasicAuthConfig basicAuthConfig = autoBranchCreationConfigCFN.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      autoBranchCreationConfig.enableBasicAuth(basicAuthConfig.getEnableBasicAuth())
              .basicAuthCredentials(getBasicAuthCredentialsSDK(basicAuthConfig));
    }
    return autoBranchCreationConfig.build();
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
