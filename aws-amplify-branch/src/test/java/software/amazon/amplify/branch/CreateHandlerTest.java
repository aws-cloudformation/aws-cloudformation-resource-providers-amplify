package software.amazon.amplify.branch;

import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.Branch;
import software.amazon.awssdk.services.amplify.model.CreateBranchRequest;
import software.amazon.awssdk.services.amplify.model.CreateBranchResponse;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
import software.amazon.awssdk.services.amplify.model.GetBranchRequest;
import software.amazon.awssdk.services.amplify.model.GetBranchResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<AmplifyClient> proxyClient;

    @Mock
    AmplifyClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(AmplifyClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down(TestInfo testInfo) {
        if (testInfo.getTags().contains("SkipCleanup")) {
            return;
        }
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        stubProxyClient();
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .branchName(BRANCH_NAME)
                .basicAuthConfig(BASIC_AUTH_CONFIG)
                .environmentVariables(ENV_VARS_CFN)
                .tags(TAGS_CFN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel expected = ResourceModel.builder()
                .appId(APP_ID)
                .arn(BRANCH_ARN)
                .branchName(BRANCH_NAME)
                .environmentVariables(ENV_VARS_CFN)
                .tags(TAGS_CFN)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expected);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_MinimalBranch() {
        Branch branchMock = Branch.builder().branchArn(BRANCH_ARN).branchName(BRANCH_NAME).build();
        CreateBranchResponse createBranchResponseMock = CreateBranchResponse.builder().branch(branchMock).build();
        GetBranchResponse getBranchResponseMock = GetBranchResponse.builder().branch(branchMock).build();
        stubProxyClient(createBranchResponseMock, getBranchResponseMock);
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .branchName(BRANCH_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel expected = ResourceModel.builder()
                .appId(APP_ID)
                .arn(BRANCH_ARN)
                .branchName(BRANCH_NAME)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expected);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_initializeModel() {
        stubProxyClientGet();
        final ReadHandler handler = new ReadHandler();

        // Model without arn causes CfnNotFoundException
        final ResourceModel modelNoArn = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request1 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelNoArn)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request1, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotFoundException.class);

        // Model with arn but without appId or branchName gets appId and branchName populated successfully
        final ResourceModel modelWithArn = ResourceModel.builder().arn(BRANCH_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request2 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithArn)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request2,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel responseModel = response.getResourceModel();

        assertThat(responseModel.getAppId()).isEqualTo(APP_ID);
        assertThat(responseModel.getBranchName()).isEqualTo(BRANCH_NAME);
    }

    @Test
    @Tag("SkipCleanup")
    public void handleRequest_InvalidModelProps() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().arn(BRANCH_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class)
                .hasMessageContaining("Attempted to provide value to a read-only property");
    }

    private void stubProxyClientGet() {
        Branch branchMock = Branch.builder().branchArn(BRANCH_ARN).branchName(BRANCH_NAME).build();
        GetBranchResponse getBranchResponseMock = GetBranchResponse.builder().branch(branchMock).build();
        when(proxyClient.client().getBranch(any(GetBranchRequest.class)))
                .thenReturn(getBranchResponseMock);
    }

    private void stubProxyClient() {
        Branch branchMock = Branch.builder()
                .branchArn(BRANCH_ARN)
                .branchName(BRANCH_NAME)
                .basicAuthCredentials(Translator.getBasicAuthCredentialsSDK(BASIC_AUTH_CONFIG))
                .environmentVariables(Translator.getEnvironmentVariablesSDK(ENV_VARS_CFN))
                .tags(Translator.getTagsSDK(TAGS_CFN))
                .build();

        CreateBranchResponse createBranchResponseMock = CreateBranchResponse.builder().branch(branchMock).build();
        GetBranchResponse getBranchResponseMock = GetBranchResponse.builder().branch(branchMock).build();
        stubProxyClient(createBranchResponseMock, getBranchResponseMock);
    }

    private void stubProxyClient(CreateBranchResponse createBranchResponseMock, GetBranchResponse getBranchResponseMock) {
        when(proxyClient.client().createBranch(any(CreateBranchRequest.class)))
                .thenReturn(createBranchResponseMock);

        when(proxyClient.client().getBranch(any(GetBranchRequest.class)))
                .thenReturn(getBranchResponseMock);
    }
}
