package software.amazon.amplify.app;

import java.time.Duration;

import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.CreateAppRequest;
import software.amazon.awssdk.services.amplify.model.CreateAppResponse;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
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
import org.junit.jupiter.api.Tag;
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
    AmplifyClient amplifyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        amplifyClient = mock(AmplifyClient.class);
        proxyClient = MOCK_PROXY(proxy, amplifyClient);
    }

    @AfterEach
    public void tear_down(TestInfo testInfo) {
        if (testInfo.getTags().contains("SkipCleanup")) {
            return;
        }
        verify(amplifyClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(amplifyClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        stubProxyClient();
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .name(APP_NAME)
                .customRules(CUSTOM_RULES_CFN)
                .environmentVariables(ENV_VARS_CFN)
                .basicAuthConfig(BASIC_AUTH_CONFIG)
                .autoBranchCreationConfig(AUTO_BRANCH_CREATION_CONFIG)
                .tags(TAGS_CFN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel expected = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .appName(APP_NAME)
                .customRules(CUSTOM_RULES_CFN)
                .environmentVariables(ENV_VARS_CFN)
                .name(APP_NAME)
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
    public void handleRequest_SimpleSuccess_MinimalApp() {
        App appMock = App.builder().appArn(APP_ARN).appId(APP_ID).name(APP_NAME).build();
        CreateAppResponse createAppResponseMock = CreateAppResponse.builder().app(appMock).build();
        GetAppResponse getAppResponseMock = GetAppResponse.builder().app(appMock).build();
        stubProxyClient(createAppResponseMock, getAppResponseMock);
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .name(APP_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel expected = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .appName(APP_NAME)
                .name(APP_NAME)
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
    public void handleRequest_SimpleSuccess_DisabledOrEmptyProps() {
        App appMock = App.builder().appArn(APP_ARN).appId(APP_ID).name(APP_NAME).build();
        CreateAppResponse createAppResponseMock = CreateAppResponse.builder().app(appMock).build();
        GetAppResponse getAppResponseMock = GetAppResponse.builder().app(appMock).build();
        stubProxyClient(createAppResponseMock, getAppResponseMock);
        final CreateHandler handler = new CreateHandler();
        final AutoBranchCreationConfig autoBranchCreationConfig = AutoBranchCreationConfig.builder()
                .enableAutoBranchCreation(true)
                .autoBranchCreationPatterns(null)
                .build();

        final ResourceModel model = ResourceModel.builder()
                .name(APP_NAME)
                .basicAuthConfig(DISABLED_BASIC_AUTH_CONFIG)
                .enableBranchAutoDeletion(false)
                .autoBranchCreationConfig(autoBranchCreationConfig)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel expected = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .appName(APP_NAME)
                .name(APP_NAME)
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

        // Model with arn but without appId gets appId populated successfully
        final ResourceModel modelWithArn = ResourceModel.builder().arn(APP_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request2 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithArn)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request2,
                new CallbackContext(), proxyClient, logger);

        assertThat(response.getResourceModel().getAppId()).isEqualTo(APP_ID);
    }

    @Test
    @Tag("SkipCleanup")
    public void handleRequest_InvalidBasicAuthSettings() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().name(APP_NAME).basicAuthConfig(INVALID_BASIC_AUTH_CONFIG).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class)
                .hasMessageContaining("Username or Password cannot be empty");
    }

    @Test
    @Tag("SkipCleanup")
    public void handleRequest_InvalidModelProps() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().arn(APP_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class)
                .hasMessageContaining("Attempted to provide value to a read-only property");
    }

    private void stubProxyClient() {
        App appMock = App.builder()
                        .appArn(APP_ARN)
                        .appId(APP_ID)
                        .name(APP_NAME)
                        .customRules(Translator.getCustomRulesSDK(CUSTOM_RULES_CFN))
                        .environmentVariables(Translator.getEnvironmentVariablesSDK(ENV_VARS_CFN))
                        .basicAuthCredentials(Translator.getBasicAuthCredentialsSDK(BASIC_AUTH_CONFIG))
                        .autoBranchCreationConfig(Translator.getAutoBranchCreationConfigSDK(AUTO_BRANCH_CREATION_CONFIG))
                        .autoBranchCreationPatterns(AUTO_BRANCH_CREATION_CONFIG.getAutoBranchCreationPatterns())
                        .tags(Translator.getTagsSDK(TAGS_CFN))
                        .build();
        CreateAppResponse createAppResponseMock = CreateAppResponse.builder().app(appMock).build();
        GetAppResponse getAppResponseMock = GetAppResponse.builder().app(appMock).build();
        stubProxyClient(createAppResponseMock, getAppResponseMock);
    }

    private void stubProxyClientGet() {
        App appMock = App.builder().appArn(APP_ARN).appId(APP_ID).name(APP_NAME).build();
        GetAppResponse getAppResponseMock = GetAppResponse.builder().app(appMock).build();
        when(proxyClient.client().getApp(any(GetAppRequest.class)))
                .thenReturn(getAppResponseMock);
    }

    private void stubProxyClient(CreateAppResponse createAppResponseMock, GetAppResponse getAppResponseMock) {
        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenReturn(createAppResponseMock);

        when(proxyClient.client().getApp(any(GetAppRequest.class)))
                .thenReturn(getAppResponseMock);
    }
}
