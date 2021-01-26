package software.amazon.amplify.domain;

import org.apache.commons.lang3.ObjectUtils;
import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.CreateBranchResponse;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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

        if (hasReadOnlyProperties(model)) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Domain::Create", proxyClient,progress.getResourceModel(),
                        progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToCreateRequest)
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

    private boolean hasReadOnlyProperties(final ResourceModel model) {
        return ObjectUtils.anyNotNull(model.getArn(), model.getCertificateRecord(), model.getDomainStatus(),
                model.getStatusReason());
    }

    private ResourceModel handleCreateResponse(final CreateDomainAssociationResponse createDomainAssociationResponse,
                                               final ResourceModel model) {
        setResourceModelId(model, createDomainAssociationResponse.domainAssociation());
        return model;
    }
}
