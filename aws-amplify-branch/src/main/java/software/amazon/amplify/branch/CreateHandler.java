package software.amazon.amplify.branch;

import org.apache.commons.lang3.ObjectUtils;
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
        logger.log("INFO: requesting with model: " + model);

        if (model.getArn() != null) {
            throw new CfnInvalidRequestException(String.format("Attempted to provide value to a read-only property: %s", model.getArn()));
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-Branch::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((createBranchRequest, proxyInvocation) -> {
                        CreateBranchResponse createBranchResponse = (CreateBranchResponse) ClientWrapper.execute(
                                proxy,
                                createBranchRequest,
                                proxyInvocation.client()::createBranch,
                                ResourceModel.TYPE_NAME,
                                model.getArn(),
                                logger
                        );
                        setResourceModelId(model, createBranchResponse.branch());
                        return createBranchResponse;
                    })
                    .progress()
               )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
