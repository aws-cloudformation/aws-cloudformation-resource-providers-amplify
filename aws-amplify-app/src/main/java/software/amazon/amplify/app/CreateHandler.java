package software.amazon.amplify.app;

import org.apache.commons.lang3.ObjectUtils;
import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.CreateAppResponse;
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

        if (hasReadOnlyProperties(model)) {
            throw new CfnInvalidRequestException("Create request includes at least one read-only property.");
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-App::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((createAppRequest, proxyInvocation) -> (CreateAppResponse) ClientWrapper.execute(
                            proxy,
                            createAppRequest,
                            proxyInvocation.client()::createApp,
                            ResourceModel.TYPE_NAME, model.getAppId(),
                            logger
                    ))
                    .done(createAppResponse -> ProgressEvent.defaultSuccessHandler(handleCreateResponse(createAppResponse, model)))
                );
    }

    private ResourceModel handleCreateResponse(final CreateAppResponse createAppResponse,
                                               final ResourceModel model) {
        setResourceModelId(model, createAppResponse.app());
        logger.log("INFO: returning model: " + model);
        return model;
    }
}
