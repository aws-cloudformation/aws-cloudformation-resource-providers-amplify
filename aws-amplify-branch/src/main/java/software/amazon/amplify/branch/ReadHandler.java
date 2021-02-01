package software.amazon.amplify.branch;


import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.GetBranchResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
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

        return proxy.initiate("AWS-Amplify-Branch::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getBranchRequest, proxyInvocation) -> (GetBranchResponse) ClientWrapper.execute(
                        proxy,
                        getBranchRequest,
                        proxyInvocation.client()::getBranch,
                        ResourceModel.TYPE_NAME,
                        model.getArn(),
                        logger
                ))
                .done(getBranchResponse -> {
                    logger.log("INFO: returning model: " + model);
                    return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getBranchResponse));
                });
    }
}
