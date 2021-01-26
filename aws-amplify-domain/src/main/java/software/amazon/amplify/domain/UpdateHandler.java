package software.amazon.amplify.domain;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.DomainStatus;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.UpdateDomainAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<AmplifyClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (hasReadOnlyProperties(model)) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Domain::Update", proxyClient, model, progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToUpdateRequest)
                    .makeServiceCall((updateDomainAssociationRequest, proxyInvocation) -> (UpdateDomainAssociationResponse) ClientWrapper.execute(
                        proxy,
                        updateDomainAssociationRequest,
                        proxyInvocation.client()::updateDomainAssociation,
                        ResourceModel.TYPE_NAME,
                        model.getArn(),
                        logger
                    ))
                    .stabilize((awsRequest, awsResponse, client, resourceModel, context) -> isStabilized(proxy, proxyClient,
                            resourceModel, logger))
                    .done(updateDomainAssociationResponse -> ProgressEvent.defaultSuccessHandler(handleUpdateResponse(updateDomainAssociationResponse, model)))
            );
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
            // domainDO status can only be UPDATING post update call, or AVAILABLE once cloudfront update is successful
            case UPDATING:
                logger.log(String.format("%s UPDATE stabilization domainStatus: %s", domainInfo, domainStatus));
                return false;
            case AVAILABLE:
                logger.log(String.format("%s UPDATE has been stabilized.", domainInfo));
                return true;
            case FAILED:
                final String FAILURE_REASON = domainAssociation.statusReason();
                logger.log(String.format("%s UPDATE stabilization failed: %s", domainInfo, FAILURE_REASON));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn(), new CfnGeneralServiceException(FAILURE_REASON));
            default:
                logger.log(String.format("%s UPDATE stabilization failed thrown due to invalid status: %s", domainInfo, domainStatus));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn());
        }
    }

    private ResourceModel handleUpdateResponse(final UpdateDomainAssociationResponse updateDomainAssociationResponse,
                                               final ResourceModel model) {
        setResourceModelId(model, updateDomainAssociationResponse.domainAssociation());
        return model;
    }
}
