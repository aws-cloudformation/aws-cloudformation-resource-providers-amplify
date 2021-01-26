package software.amazon.amplify.domain;

import org.apache.commons.lang3.ObjectUtils;
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
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;


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
        // ~99.9% ACM certs issue within 5m, and largely within the first 10-20s: https://tt.amazon.com/0305154373
        final Constant CONSTANT = Constant.of().timeout(Duration.ofMinutes(5L)).delay(Duration.ofSeconds(10L)).build();

        if (hasReadOnlyProperties(model)) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Domain::Create", proxyClient,progress.getResourceModel(),
                        progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .backoffDelay(CONSTANT)
                    .makeServiceCall((createBranchRequest, proxyInvocation) -> (CreateDomainAssociationResponse) ClientWrapper.execute(
                        proxy,
                        createBranchRequest,
                        proxyInvocation.client()::createDomainAssociation,
                        ResourceModel.TYPE_NAME,
                        model.getArn(),
                        logger
                    ))
                    .stabilize((awsRequest, awsResponse, client, resourceModel, context) -> isStabilized(proxy, proxyClient,
                            resourceModel, logger))
                    .done(createDomainAssociationResponse -> ProgressEvent.defaultSuccessHandler(handleCreateResponse(createDomainAssociationResponse, model)))
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

    private ResourceModel handleCreateResponse(final CreateDomainAssociationResponse createDomainAssociationResponse,
                                               final ResourceModel model) {
        setResourceModelId(model, createDomainAssociationResponse.domainAssociation());
        return model;
    }
}
