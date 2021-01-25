package software.amazon.amplify.branch;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.CreateBranchResponse;
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
        if (model.getArn() != null) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Branch::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((createBranchRequest, proxyInvocation) -> (CreateBranchResponse) ClientWrapper.execute(
                            proxy,
                            createBranchRequest,
                            proxyInvocation.client()::createBranch,
                            ResourceModel.TYPE_NAME,
                            model.getArn(),
                            logger
                    ))
                    .done(createBranchResponse -> ProgressEvent.defaultSuccessHandler(handleCreateResponse(createBranchResponse, model)))
               );
    }

    private ResourceModel handleCreateResponse(final CreateBranchResponse createBranchResponse,
                                               final ResourceModel model) {
        setResourceModelId(model, createBranchResponse.branch());
        return model;
    }
}
