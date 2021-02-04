package software.amazon.amplify.branch;

import lombok.NonNull;
import software.amazon.amplify.common.utils.ArnUtils;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected void setResourceModelId(@NonNull final ResourceModel model, @NonNull final Branch branch) {
    final String SPLIT_KEY = "/branches/";
    model.setArn(branch.branchArn());
    model.setAppId(ArnUtils.getAppId(branch.branchArn(), SPLIT_KEY));
    model.setBranchName(branch.branchName());
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<AmplifyClient> proxyClient,
    final Logger logger);
}
