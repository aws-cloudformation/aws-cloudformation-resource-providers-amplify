package software.amazon.amplify.app;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

public class Translator {
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
      createAppRequest.customRules(getCustomRules(customRules));
    }
    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      createAppRequest.environmentVariables(getEnvironmentVariables(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      createAppRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      createAppRequest.basicAuthCredentials(getBasicAuthCredentials(basicAuthConfig));
    }
    software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfigCFN = model.getAutoBranchCreationConfig();
    if (autoBranchCreationConfigCFN != null) {
      createAppRequest.enableAutoBranchCreation(autoBranchCreationConfigCFN.getEnableAutoBranchCreation());
      if (autoBranchCreationConfigCFN.getEnableAutoBranchCreation()) {
        createAppRequest.autoBranchCreationConfig(getAutoBranchCreationConfig(autoBranchCreationConfigCFN));
        List<String> autoBranchCreationPatterns = autoBranchCreationConfigCFN.getAutoBranchCreationPatterns();
        if (CollectionUtils.isNotEmpty(autoBranchCreationPatterns)) {
          createAppRequest.autoBranchCreationPatterns(autoBranchCreationPatterns);
        }
      }
    }

    List<Tag> appTags = model.getTags();
    if (CollectionUtils.isNotEmpty(appTags)) {
      createAppRequest.tags(getTags(appTags));
    }
    return createAppRequest.build();
  }

  static GetAppRequest translateToReadRequest(final ResourceModel model) {
    return GetAppRequest.builder()
            .appId(model.getAppId())
            .build();
  }

  static ResourceModel translateFromReadResponse(final GetAppResponse response) {
    App app = response.app();

    ResourceModel.ResourceModelBuilder appModelBuilder = ResourceModel.builder()
            .appId(app.appId())
            .arn(app.appArn())
            .buildSpec(app.buildSpec())
            .customHeaders(app.customHeaders())
            .description(app.description())
            .defaultDomain(app.defaultDomain())
            .enableBranchAutoDeletion(app.enableBranchAutoDeletion())
            .iAMServiceRole(app.iamServiceRoleArn())
            .name(app.name())
            .repository(app.repository());

    Map<String, String> appEnvVars = app.environmentVariables();
    if (MapUtils.isNotEmpty(appEnvVars)) {
      appModelBuilder.environmentVariables(getEnvironmentVariablesCFN(appEnvVars));
    }

    AutoBranchCreationConfig autoBranchCreationConfig = app.autoBranchCreationConfig();
    if (autoBranchCreationConfig != null) {
      software.amazon.amplify.app.AutoBranchCreationConfig.AutoBranchCreationConfigBuilder autoBranchCreationConfigCFN =
              software.amazon.amplify.app.AutoBranchCreationConfig.builder()
                .autoBranchCreationPatterns(app.autoBranchCreationPatterns())
                .basicAuthConfig(getBasicAuthConfig(autoBranchCreationConfig.basicAuthCredentials(), autoBranchCreationConfig.enableBasicAuth()))
                .buildSpec(autoBranchCreationConfig.buildSpec())
                .enableAutoBranchCreation(app.enableAutoBranchCreation())
                .enableAutoBuild(autoBranchCreationConfig.enableAutoBuild())
                .enablePerformanceMode(autoBranchCreationConfig.enablePerformanceMode())
                .enablePullRequestPreview(autoBranchCreationConfig.enablePullRequestPreview())
                .pullRequestEnvironmentName(autoBranchCreationConfig.pullRequestEnvironmentName())
                .stage(autoBranchCreationConfig.stageAsString());
      Map<String, String> autoBranchEnvVars = autoBranchCreationConfig.environmentVariables();
      if (MapUtils.isNotEmpty(autoBranchEnvVars)) {
        autoBranchCreationConfigCFN.environmentVariables(getEnvironmentVariablesCFN(autoBranchCreationConfig.environmentVariables()));
      }
      appModelBuilder.autoBranchCreationConfig(autoBranchCreationConfigCFN.build());
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
  }

  static DeleteAppRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteAppRequest.builder()
            .appId(model.getAppId())
            .build();
  }

  static UpdateAppRequest translateToUpdateRequest(final ResourceModel model) {
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
      updateAppRequest.customRules(getCustomRules(customRules));
    }
    List<EnvironmentVariable> environmentVariables = model.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(environmentVariables)) {
      updateAppRequest.environmentVariables(getEnvironmentVariables(environmentVariables));
    }
    BasicAuthConfig basicAuthConfig = model.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      updateAppRequest.enableBasicAuth(basicAuthConfig.getEnableBasicAuth());
      updateAppRequest.basicAuthCredentials(getBasicAuthCredentials(basicAuthConfig));
    }
    software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfig = model.getAutoBranchCreationConfig();
    if (autoBranchCreationConfig != null) {
      updateAppRequest.enableAutoBranchCreation(autoBranchCreationConfig.getEnableAutoBranchCreation());
      updateAppRequest.autoBranchCreationConfig(getAutoBranchCreationConfig(autoBranchCreationConfig));
    }
    return updateAppRequest.build();
  }

  static ListAppsRequest translateToListRequest(final String nextToken) {
    return ListAppsRequest.builder()
            .nextToken(nextToken)
            .build();
  }

  static List<ResourceModel> translateFromListRequest(final ListAppsResponse response) {
    return streamOfOrEmpty(response.apps())
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
  public static Map<String, String> getTags(@NonNull final List<Tag> tags) {
    Map<String, String> tagMap = new HashMap<>();
    for (Tag tag : tags) {
      tagMap.put(tag.getKey(), tag.getValue());
    }
    return tagMap;
  }

  private static List<CustomRule> getCustomRules(@NonNull final List<software.amazon.amplify.app.CustomRule> customRulesCFN) {
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

  private static Map<String, String> getEnvironmentVariables(@NonNull final List<EnvironmentVariable> envVarsCFN) {
    Map<String, String> envVars = new HashMap<>();
    for (EnvironmentVariable envVarCFN : envVarsCFN) {
      envVars.put(envVarCFN.getName(), envVarCFN.getValue());
    }
    return envVars;
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

  private static String getBasicAuthCredentials(@NonNull BasicAuthConfig basicAuthConfig) {
    final String userInfo = String.format("%s:%s", basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
    return Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
  }

  private static BasicAuthConfig getBasicAuthConfig(@NonNull String basicAuthConfigString, @NonNull Boolean isEnabled) {
    final String DELIMITER = ":";
    final String userInfo = Arrays.toString(Base64.getDecoder().decode(basicAuthConfigString));
    return BasicAuthConfig.builder()
            .username(userInfo.split(DELIMITER)[0])
            .password(userInfo.split(DELIMITER)[1])
            .enableBasicAuth(isEnabled)
            .build();
  }

  private static AutoBranchCreationConfig getAutoBranchCreationConfig(@NonNull final software.amazon.amplify.app.AutoBranchCreationConfig autoBranchCreationConfigCFN) {

    AutoBranchCreationConfig.Builder autoBranchCreationConfig = AutoBranchCreationConfig.builder()
            .buildSpec(autoBranchCreationConfigCFN.getBuildSpec())
            .stage(autoBranchCreationConfigCFN.getStage())
            .enableAutoBuild(autoBranchCreationConfigCFN.getEnableAutoBuild())
            .enablePullRequestPreview(autoBranchCreationConfigCFN.getEnablePullRequestPreview())
            .enablePerformanceMode(autoBranchCreationConfigCFN.getEnablePerformanceMode())
            .pullRequestEnvironmentName(autoBranchCreationConfigCFN.getPullRequestEnvironmentName());

    List<EnvironmentVariable> envVarsCFN = autoBranchCreationConfigCFN.getEnvironmentVariables();
    if (CollectionUtils.isNotEmpty(envVarsCFN)) {
      autoBranchCreationConfig.environmentVariables(getEnvironmentVariables(envVarsCFN));
    }
    BasicAuthConfig basicAuthConfig = autoBranchCreationConfigCFN.getBasicAuthConfig();
    if (basicAuthConfig != null) {
      autoBranchCreationConfig.enableBasicAuth(basicAuthConfig.getEnableBasicAuth())
              .basicAuthCredentials(getBasicAuthCredentials(basicAuthConfig));
    }

    return autoBranchCreationConfig.build();
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
