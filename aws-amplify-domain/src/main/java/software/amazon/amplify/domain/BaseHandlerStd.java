package software.amazon.amplify.domain;

import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.NotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

  protected boolean hasReadOnlyProperties(final ResourceModel model) {
    return ObjectUtils.anyNotNull(model.getArn(), model.getDomainStatus(),
            model.getStatusReason());
  }

  protected GetDomainAssociationResponse checkIfResourceExists(GetDomainAssociationRequest getDomainAssociationRequest,
                                                               ProxyClient<AmplifyClient> client,
                                                               Logger logger) {
    GetDomainAssociationResponse response = null;
    try {
      logger.log(String.format("Checking if DomainAssociation already exists for request: " + getDomainAssociationRequest));
      response = client.injectCredentialsAndInvokeV2(getDomainAssociationRequest, client.client()::getDomainAssociation);
    } catch (final NotFoundException e) {
      // proceed
    } catch (final Exception e) {
      throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
    }
    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
    if (response != null) {
      throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, getDomainAssociationRequest.domainName());
    }
    return response;
  }
}
