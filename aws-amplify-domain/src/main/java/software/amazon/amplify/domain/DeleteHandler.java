package software.amazon.amplify.domain;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DeleteDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<AmplifyClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        logger.log("INFO: requesting with model: " + model);

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Domain::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteDomainAssociationRequest, proxyInvocation) -> (DeleteDomainAssociationResponse) ClientWrapper.execute(
                            proxy,
                            deleteDomainAssociationRequest,
                            proxyInvocation.client()::deleteDomainAssociation,
                            ResourceModel.TYPE_NAME,
                            model.getArn(),
                            logger
                    ))
                    .stabilize((awsRequest, awsResponse, client, resourceModel, context) -> isStabilized(proxy, proxyClient,
                            model, logger))
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private boolean isStabilized(final AmazonWebServicesClientProxy proxy,
                                 final ProxyClient<AmplifyClient> proxyClient,
                                 final ResourceModel model,
                                 final Logger logger) {
        final String domainInfo = String.format("%s - %s", model.getAppId(), model.getDomainName());

        try {
            final GetDomainAssociationRequest getDomainAssociationRequest = GetDomainAssociationRequest.builder()
                    .appId(model.getAppId())
                    .domainName(model.getDomainName())
                    .build();
            ClientWrapper.execute(
                    proxy,
                    getDomainAssociationRequest,
                    proxyClient.client()::getDomainAssociation,
                    ResourceModel.TYPE_NAME,
                    model.getArn(),
                    logger);
            logger.log(String.format("%s DELETE stabilization still in progress", domainInfo));
            return false;
        } catch (final CfnNotFoundException e) {
            logger.log(String.format("%s DELETE stabilization complete", domainInfo));
            return true;
        } catch (final AwsServiceException e) {
            logger.log(String.format("%s DELETE stabilization failed: %s", domainInfo, e));
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn());
        }
    }
}
