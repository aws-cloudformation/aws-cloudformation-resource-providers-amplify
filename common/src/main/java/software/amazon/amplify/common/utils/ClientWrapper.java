package software.amazon.amplify.common.utils;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.amplify.model.BadRequestException;
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

import java.util.function.Function;

public final class ClientWrapper {
    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> AwsResponse execute(
            final AmazonWebServicesClientProxy clientProxy,
            final RequestT request,
            final Function<RequestT, ResultT> requestFunction,
            final String resourceTypeName,
            final Logger logger) {
        return execute(clientProxy, request, requestFunction, resourceTypeName, "", logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> AwsResponse execute(
            final AmazonWebServicesClientProxy clientProxy,
            final RequestT request,
            final Function<RequestT, ResultT> requestFunction,
            final String resourceTypeName,
            final String resourceTypeId,
            final Logger logger) {
        try {
            logger.log("Invoking with request: " + request.toString());
            return clientProxy.injectCredentialsAndInvokeV2(request, requestFunction);
        } catch (NotFoundException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnNotFoundException(resourceTypeName, resourceTypeId);
        } catch (InternalFailureException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnInternalFailureException(e);
        } catch (LimitExceededException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnServiceLimitExceededException(resourceTypeName, e.getMessage());
        } catch (BadRequestException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnInvalidRequestException(e.getMessage(), e);
        } catch (UnauthorizedException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnAccessDeniedException(e);
        } catch (AwsServiceException e) {
            logger.log("ERROR: " + e.getMessage());
            throw new CfnGeneralServiceException(e);
        }
    }
}
