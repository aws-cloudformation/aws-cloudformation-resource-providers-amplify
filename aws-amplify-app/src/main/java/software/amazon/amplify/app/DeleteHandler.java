package software.amazon.amplify.app;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DeleteAppResponse;
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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-App::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteAppRequest, proxyInvocation) -> (DeleteAppResponse) ClientWrapper.execute(
                            proxy,
                            deleteAppRequest,
                            proxyInvocation.client()::deleteApp,
                            ResourceModel.TYPE_NAME,
                            model.getAppId(),
                            logger
                    ))
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
