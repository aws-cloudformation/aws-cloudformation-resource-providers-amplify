package software.amazon.amplify.domain;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.DomainStatus;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {
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

        if (hasReadOnlyProperties(model)) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                    proxy.initiate("AWS-Amplify-Domain::Create", proxyClient,progress.getResourceModel(),
                            progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToCreateRequest)
                        .makeServiceCall((createDomainAssociationRequest, proxyInvocation) -> {
                            checkIfResourceExists(model, proxyClient, logger);
                            return (CreateDomainAssociationResponse) ClientWrapper.execute(
                                    proxy,
                                    createDomainAssociationRequest,
                                    proxyInvocation.client()::createDomainAssociation,
                                    ResourceModel.TYPE_NAME,
                                    model.getArn(),
                                    logger
                            );
                        })
                        .stabilize((awsRequest, awsResponse, client, resourceModel, context) -> isStabilized(proxy, proxyClient,
                                model, logger))
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private boolean isStabilized(final AmazonWebServicesClientProxy proxy,
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

        final String domainInfo = String.format("%s - %s", model.getAppId(), model.getDomainName());
        final DomainAssociation domainAssociation = getDomainAssociationResponse.domainAssociation();
        final DomainStatus domainStatus = domainAssociation.domainStatus();

        switch (domainStatus) {
            case CREATING:
            case REQUESTING_CERTIFICATE:
            case IN_PROGRESS:
                logger.log(String.format("%s CREATE stabilization domainStatus: %s", domainInfo, domainStatus));
                return false;
            case PENDING_VERIFICATION:
            case PENDING_DEPLOYMENT:
            case AVAILABLE:
            case UPDATING:
                logger.log(String.format("%s CREATE has been stabilized.", domainInfo));
                Translator.translateFromCreateOrUpdateResponse(model, getDomainAssociationResponse.domainAssociation());
                return true;
            case FAILED:
                final String FAILURE_REASON = domainAssociation.statusReason();
                logger.log(String.format("%s CREATE stabilization failed: %s", domainInfo, FAILURE_REASON));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn(), new CfnGeneralServiceException(FAILURE_REASON));
            default:
                logger.log(String.format("%s CREATE stabilization failed thrown due to invalid status: %s", domainInfo, domainStatus));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn());
        }
    }
}
