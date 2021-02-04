package software.amazon.amplify.domain;

import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.amplify.common.utils.ArnUtils;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
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

  protected GetDomainAssociationResponse checkIfResourceExists(ResourceModel model,
                                                               ProxyClient<AmplifyClient> client,
                                                               Logger logger) {
    GetDomainAssociationResponse response = null;
    try {
      logger.log(String.format("Checking if %s already exists for appId: %s, domainName: %s",
              ResourceModel.TYPE_NAME, model.getAppId(), model.getDomainName()));
      response = client.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), client.client()::getDomainAssociation);
    } catch (final NotFoundException e) {
      // proceed
    } catch (final Exception e) {
      throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
    }
    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
    if (response != null) {
      throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getDomainName());
    }
    // should always be null
    return response;
  }

  protected void setResourceModelId(@NonNull final ResourceModel model, @NonNull final DomainAssociation domainAssociation) {
    final String SPLIT_KEY = "/domains/";
    model.setArn(domainAssociation.domainAssociationArn());
    model.setAppId(ArnUtils.getAppId(domainAssociation.domainAssociationArn(), SPLIT_KEY));
    model.setDomainName(domainAssociation.domainName());
  }
}
