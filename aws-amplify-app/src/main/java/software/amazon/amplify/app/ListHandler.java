package software.amazon.amplify.app;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.ListAppsRequest;
import software.amazon.awssdk.services.amplify.model.ListAppsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<AmplifyClient> proxyClient,
        final Logger logger) {

        final ListAppsRequest listAppsRequest = Translator.translateToListRequest(request.getNextToken());
        ListAppsResponse listAppsResponse = (ListAppsResponse) ClientWrapper.execute(
                proxy,
                listAppsRequest,
                proxyClient.client()::listApps,
                ResourceModel.TYPE_NAME,
                request.getDesiredResourceState().getAppId(),
                logger
        );
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListRequest(listAppsResponse))
            .nextToken(listAppsResponse.nextToken())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
