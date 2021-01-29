package software.amazon.amplify.domain;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.ListDomainAssociationsRequest;
import software.amazon.awssdk.services.amplify.model.ListDomainAssociationsResponse;
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

        final ListDomainAssociationsRequest listBranchesRequest = Translator.translateToListRequest(request.getDesiredResourceState(),
                request.getNextToken());
        ListDomainAssociationsResponse listBranchesResponse = (ListDomainAssociationsResponse) ClientWrapper.execute(
                proxy,
                listBranchesRequest,
                proxyClient.client()::listDomainAssociations,
                ResourceModel.TYPE_NAME,
                logger
        );
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(Translator.translateFromListRequest(listBranchesResponse))
                .nextToken(listBranchesResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
