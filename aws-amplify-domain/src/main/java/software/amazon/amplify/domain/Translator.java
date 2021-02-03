package software.amazon.amplify.domain;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.amplify.common.utils.ArnUtils;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.DeleteDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.ListDomainAssociationsRequest;
import software.amazon.awssdk.services.amplify.model.ListDomainAssociationsResponse;
import software.amazon.awssdk.services.amplify.model.SubDomain;
import software.amazon.awssdk.services.amplify.model.UpdateDomainAssociationRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;

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
  private static final String ARN_SPLIT_KEY = "/domains/";

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

  static ResourceModel translateFromCreateOrUpdateResponse(final ResourceModel model, final DomainAssociation domainAssociation) {
    model.setArn(domainAssociation.domainAssociationArn());
    model.setDomainStatus(domainAssociation.domainStatusAsString());
    return model;
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return getDomainAssociationRequest the aws service request to describe a resource
   */
  static GetDomainAssociationRequest translateToReadRequest(final ResourceModel model) {
    initializeModel(model);
    return GetDomainAssociationRequest.builder()
            .appId(model.getAppId())
            .domainName(model.getDomainName())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param getDomainAssociationResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetDomainAssociationResponse getDomainAssociationResponse) {
    final DomainAssociation domainAssociation = getDomainAssociationResponse.domainAssociation();

    ResourceModel.ResourceModelBuilder domainAssociationModelBuilder = ResourceModel.builder()
            .appId(ArnUtils.getAppId(domainAssociation.domainAssociationArn(), ARN_SPLIT_KEY))
            .arn(domainAssociation.domainAssociationArn())
            .certificateRecord(domainAssociation.certificateVerificationDNSRecord())
            .domainName(domainAssociation.domainName())
            .enableAutoSubDomain(domainAssociation.enableAutoSubDomain())
            .autoSubDomainIAMRole(domainAssociation.autoSubDomainIAMRole())
            .domainStatus(domainAssociation.domainStatusAsString())
            .statusReason(domainAssociation.statusReason());

    List<String> autoSubDomainCreationPatterns = domainAssociation.autoSubDomainCreationPatterns();
    if (CollectionUtils.isNotEmpty(autoSubDomainCreationPatterns)) {
      domainAssociationModelBuilder.autoSubDomainCreationPatterns(autoSubDomainCreationPatterns);
    }

    List<SubDomain> subDomainsSDK = domainAssociation.subDomains();
    if (CollectionUtils.isNotEmpty(subDomainsSDK)) {
        domainAssociationModelBuilder.subDomainSettings(getSubDomainSettingsCFN(subDomainsSDK));
    }
    return domainAssociationModelBuilder.build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return deleteDomainAssociationRequest the aws service request to delete a resource
   */
  static DeleteDomainAssociationRequest translateToDeleteRequest(final ResourceModel model) {
    initializeModel(model);
    return DeleteDomainAssociationRequest.builder()
            .appId(model.getAppId())
            .domainName(model.getDomainName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return updateDomainAssociationRequest the aws service request to modify a resource
   */
  static UpdateDomainAssociationRequest translateToUpdateRequest(final ResourceModel model) {
    initializeModel(model);
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
   * @return listDomainAssociationsRequest the aws service request to list resources within aws account
   */
  static ListDomainAssociationsRequest translateToListRequest(final ResourceModel model, String nextToken) {
    return ListDomainAssociationsRequest.builder()
            .appId(model.getAppId())
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param listDomainAssociationsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListDomainAssociationsResponse listDomainAssociationsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
    return streamOfOrEmpty(listDomainAssociationsResponse.domainAssociations())
        .map(resource -> ResourceModel.builder()
            .arn(resource.domainAssociationArn())
            .domainName(resource.domainName())
            .build())
        .collect(Collectors.toList());
  }

  /*
   * Helpers
   */
  private static void initializeModel(final ResourceModel model) {
    if (model.getAppId() == null || model.getDomainName() == null) {
      String arn = model.getArn();
      if (arn == null) {
        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
      }
      try {
        model.setAppId(ArnUtils.getAppId(arn, ARN_SPLIT_KEY));
        model.setDomainName(ArnUtils.getResourceName(arn, ARN_SPLIT_KEY));
      } catch (Exception e) {
        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
      }
    }
  }

  private static List<SubDomainSetting> getSubDomainSettingsCFN(List<SubDomain> subDomainsSDK) {
    List<SubDomainSetting> subDomainSettingsCFN = new ArrayList<>();
    for (SubDomain subDomain : subDomainsSDK) {
      software.amazon.awssdk.services.amplify.model.SubDomainSetting subDomainSettingSDK = subDomain.subDomainSetting();
      subDomainSettingsCFN.add(SubDomainSetting.builder()
              .branchName(subDomainSettingSDK.branchName())
              .prefix(subDomainSettingSDK.prefix())
              .build()
      );
    }
    return subDomainSettingsCFN;
  }
  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
