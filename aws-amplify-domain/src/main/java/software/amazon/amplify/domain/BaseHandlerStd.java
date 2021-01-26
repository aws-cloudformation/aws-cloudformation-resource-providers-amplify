package software.amazon.amplify.domain;

import lombok.NonNull;
import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.DomainStatus;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<AmplifyClient> proxyClient,
    final Logger logger);

  static boolean isStabilized(final AmazonWebServicesClientProxy proxy,
                              final ProxyClient<AmplifyClient> proxyClient,
                              final ResourceModel model,
                              final Logger logger) {
    final GetDomainAssociationRequest getDomainAssociationRequest = GetDomainAssociationRequest.builder()
            .appId(model.getAppId())
            .domainName(model.getDomainName())
            .build();
    final GetDomainAssociationResponse getDomainAssociationResponse = (GetDomainAssociationResponse) ClientWrapper.execute(
            proxy,
            getDomainAssociationRequest,
            proxyClient.client()::getDomainAssociation,
            ResourceModel.TYPE_NAME,
            model.getArn(),
            logger);

    if (getDomainAssociationResponse == null) {
      logger.log(String.format("Domain %s not yet found for appId %s", model.getDomainName(), model.getAppId()));
      return false;
    }

    final String domainInfo = String.format("%s [%s]", ResourceModel.TYPE_NAME, model.getAppId());
    final DomainAssociation domainAssociation = getDomainAssociationResponse.domainAssociation();
    final DomainStatus domainStatus = domainAssociation.domainStatus();

    if (domainStatus == null) {
      logger.log(String.format("Domain status not yet populated for domain %s, appId %s", model.getDomainName(), model.getAppId()));
      return false;
    }

    switch (domainStatus) {
      case CREATING:
      case REQUESTING_CERTIFICATE:
      case IN_PROGRESS:
        logger.log(String.format("%s stabilization status: %s", domainInfo, domainStatus));
        return false;
      case PENDING_VERIFICATION:
      case PENDING_DEPLOYMENT:
      case AVAILABLE:
      case UPDATING:
        logger.log(String.format("%s has been stabilized.", domainInfo));
        return true;
      case FAILED:
        final String FAILURE_REASON = domainAssociation.statusReason();
        logger.log(String.format("%s stabilization failed: %s", domainInfo, FAILURE_REASON));
        throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn(), new CfnGeneralServiceException(FAILURE_REASON));
      default:
        logger.log(String.format("%s stabilization failed thrown due to unexpected status: %s", domainInfo, domainStatus));
        throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn());
    }
  }

  public void setResourceModelId(@NonNull final ResourceModel model, @NonNull final DomainAssociation domain) {
    model.setArn(domain.domainAssociationArn());
  }
}
