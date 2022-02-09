package software.amazon.amplify.branch;

import software.amazon.amplify.common.utils.ClientWrapper;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DeleteBranchResponse;
import software.amazon.awssdk.services.amplify.model.GetBranchRequest;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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
                        proxy.initiate("AWS-Amplify-Branch::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall((deleteBranchRequest, proxyInvocation) -> (DeleteBranchResponse) ClientWrapper.execute(
                                        proxy,
                                        deleteBranchRequest,
                                        proxyInvocation.client()::deleteBranch,
                                        ResourceModel.TYPE_NAME,
                                        model.getArn(),
                                        logger
                                )).stabilize((awsRequest, awsResponse, client, resourceModel, context) -> isStabilized(proxy, proxyClient,
                                model, logger))
                                .progress()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private boolean isStabilized(final AmazonWebServicesClientProxy proxy,
                                 final ProxyClient<AmplifyClient> proxyClient,
                                 final ResourceModel model,
                                 final Logger logger) {
        final String branchInfo = String.format("%s - %s", model.getAppId(), model.getBranchName());

        try {
            final GetBranchRequest getBranchRequest = GetBranchRequest.builder()
                    .appId(model.getAppId())
                    .branchName(model.getBranchName())
                    .build();
            ClientWrapper.execute(
                    proxy,
                    getBranchRequest,
                    proxyClient.client()::getBranch,
                    ResourceModel.TYPE_NAME,
                    model.getArn(),
                    logger);
            logger.log(String.format("%s DELETE stabilization still in progress", branchInfo));
            return false;
        } catch (final CfnNotFoundException e) {
            logger.log(String.format("%s DELETE stabilization complete", branchInfo));
            return true;
        } catch (final AwsServiceException e) {
            logger.log(String.format("%s DELETE stabilization failed: %s", branchInfo, e));
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getArn());
        }
    }
}
