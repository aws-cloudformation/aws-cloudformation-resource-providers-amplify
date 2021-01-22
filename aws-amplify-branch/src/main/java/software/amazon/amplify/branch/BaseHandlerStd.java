package software.amazon.amplify.branch;

import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.BadRequestException;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.awssdk.services.amplify.model.InternalFailureException;
import software.amazon.awssdk.services.amplify.model.LimitExceededException;
import software.amazon.awssdk.services.amplify.model.NotFoundException;
import software.amazon.awssdk.services.amplify.model.UnauthorizedException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

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

  public void setResourceModelId(@NonNull final ResourceModel model, @NonNull final Branch branch) {
    model.setArn(branch.branchArn());
    model.setBranchName(branch.branchName());
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<AmplifyClient> proxyClient,
    final Logger logger);

  protected static <RequestT extends AwsRequest, ResultT extends AwsResponse> AwsResponse execute(
          final AmazonWebServicesClientProxy clientProxy,
          RequestT request,
          Function<RequestT, ResultT> requestFunction,
          ResourceModel model,
          Logger logger) {
    try {
      logger.log("Invoking with request: " + request.toString());
      return clientProxy.injectCredentialsAndInvokeV2(request, requestFunction);
    } catch (NotFoundException e) {
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getAppId());
    } catch (InternalFailureException e) {
      throw new CfnInternalFailureException(e);
    } catch (LimitExceededException e) {
      throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage());
    } catch (BadRequestException e) {
      throw new CfnInvalidRequestException(e.getMessage(), e);
    } catch (UnauthorizedException e) {
      throw new CfnAccessDeniedException(e);
    } catch (AwsServiceException e) {
      throw new CfnGeneralServiceException(e);
    }
  }
}
