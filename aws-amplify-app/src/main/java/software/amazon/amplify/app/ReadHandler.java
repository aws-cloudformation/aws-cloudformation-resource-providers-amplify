package software.amazon.amplify.app;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
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

        return proxy.initiate("AWS-Amplify-App::Read", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((getAppRequest, proxyInvocation) -> (GetAppResponse) ClientWrapper.execute(
                    proxy,
                    getAppRequest,
                    proxyInvocation.client()::getApp,
                    ResourceModel.TYPE_NAME,
                    model.getArn(),
                    logger
            ))
            .done(getAppResponse -> {
                ResourceModel modelRet = Translator.translateFromReadResponse(getAppResponse);
                logger.log("INFO: returning model: " + modelRet);
                return ProgressEvent.defaultSuccessHandler(modelRet);
            });
    }
}
