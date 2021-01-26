package software.amazon.amplify.domain;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.UpdateDomainAssociationRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
   * @return createDomainAssociationRequest the aws service request to create a resource
   */
  static CreateDomainAssociationRequest translateToCreateRequest(final ResourceModel model) {
    final CreateDomainAssociationRequest.Builder createDomainAssociationRequest = CreateDomainAssociationRequest.builder()
            .appId(model.getAppId())
            .domainName(model.getDomainName())
            .enableAutoSubDomain(model.getEnableAutoSubDomain())
            .autoSubDomainCreationPatterns(model.getAutoSubDomainCreationPatterns())
            .autoSubDomainIAMRole(model.getAutoSubDomainIAMRole());

    List<SubDomainSetting> subDomainSettingsCFN = model.getSubDomainSettings();
    if (CollectionUtils.isNotEmpty(subDomainSettingsCFN)) {
      List<software.amazon.awssdk.services.amplify.model.SubDomainSetting> subDomainSettingsSDK = new ArrayList<>();
      for (final SubDomainSetting subDomainSettingCFN : subDomainSettingsCFN) {
        software.amazon.awssdk.services.amplify.model.SubDomainSetting subDomainSettingSDK =
                software.amazon.awssdk.services.amplify.model.SubDomainSetting.builder()
                  .prefix(subDomainSettingCFN.getPrefix())
                  .branchName(subDomainSettingCFN.getBranchName())
                  .build();
        subDomainSettingsSDK.add(subDomainSettingSDK);
      }
      createDomainAssociationRequest.subDomainSettings(subDomainSettingsSDK);
    }
    return createDomainAssociationRequest.build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static AwsRequest translateToReadRequest(final ResourceModel model) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L20-L24
    return awsRequest;
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final AwsResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L58-L73
    return ResourceModel.builder()
        //.someProperty(response.property())
        .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static AwsRequest translateToDeleteRequest(final ResourceModel model) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L33-L37
    return awsRequest;
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return updateDomainAssociationRequest the aws service request to modify a resource
   */
  static UpdateDomainAssociationRequest translateToUpdateRequest(final ResourceModel model) {
    final UpdateDomainAssociationRequest.Builder updateDomainAssociationRequest = UpdateDomainAssociationRequest.builder()
            .appId(model.getAppId())
            .domainName(model.getDomainName())
            .enableAutoSubDomain(model.getEnableAutoSubDomain())
            .autoSubDomainCreationPatterns(model.getAutoSubDomainCreationPatterns())
            .autoSubDomainIAMRole(model.getAutoSubDomainIAMRole());

    List<SubDomainSetting> subDomainSettingsCFN = model.getSubDomainSettings();
    if (CollectionUtils.isNotEmpty(subDomainSettingsCFN)) {
      List<software.amazon.awssdk.services.amplify.model.SubDomainSetting> subDomainSettingsSDK = new ArrayList<>();
      for (final SubDomainSetting subDomainSettingCFN : subDomainSettingsCFN) {
        software.amazon.awssdk.services.amplify.model.SubDomainSetting subDomainSettingSDK =
                software.amazon.awssdk.services.amplify.model.SubDomainSetting.builder()
                        .prefix(subDomainSettingCFN.getPrefix())
                        .branchName(subDomainSettingCFN.getBranchName())
                        .build();
        subDomainSettingsSDK.add(subDomainSettingSDK);
      }
      updateDomainAssociationRequest.subDomainSettings(subDomainSettingsSDK);
    }
    return updateDomainAssociationRequest.build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static AwsRequest translateToListRequest(final String nextToken) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L26-L31
    return awsRequest;
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

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
