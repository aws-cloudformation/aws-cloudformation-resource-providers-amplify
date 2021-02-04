package software.amazon.amplify.app;

import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.CreateAppResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;

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

        // Make sure the user isn't trying to assign values to read-only properties
        String disallowedVal = checkReadOnlyProperties(model);
        if (disallowedVal != null) {
            throw new CfnInvalidRequestException(String.format("Attempted to provide value to a read-only property: %s", disallowedVal));
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-App::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((createAppRequest, proxyInvocation) -> {
                        CreateAppResponse createAppResponse = (CreateAppResponse) ClientWrapper.execute(
                                proxy,
                                createAppRequest,
                                proxyInvocation.client()::createApp,
                                ResourceModel.TYPE_NAME, model.getArn(),
                                logger
                        );
                        setResourceModelId(model, createAppResponse.app());
                        return createAppResponse;
                    })
                    .progress()
                )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private String checkReadOnlyProperties(final ResourceModel model) {
        return ObjectUtils.firstNonNull(model.getAppId(), model.getDefaultDomain(), model.getArn());
    }
}
