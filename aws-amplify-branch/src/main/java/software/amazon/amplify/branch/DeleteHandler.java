package software.amazon.amplify.branch;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DeleteBranchResponse;
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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Amplify-Branch::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall((deleteBranchRequest, proxyInvocation) -> (DeleteBranchResponse) ClientWrapper.execute(
                                        proxy,
                                        deleteBranchRequest,
                                        proxyInvocation.client()::deleteBranch,
                                        ResourceModel.TYPE_NAME,
                                        model.getArn(),
                                        logger
                                ))
                                .progress()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
