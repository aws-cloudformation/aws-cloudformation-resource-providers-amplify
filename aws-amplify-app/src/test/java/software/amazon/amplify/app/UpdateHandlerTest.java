package software.amazon.amplify.app;

import java.time.Duration;

import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.amplify.model.TagResourceRequest;
import software.amazon.awssdk.services.amplify.model.TagResourceResponse;
import software.amazon.awssdk.services.amplify.model.UntagResourceRequest;
import software.amazon.awssdk.services.amplify.model.UntagResourceResponse;
import software.amazon.awssdk.services.amplify.model.UpdateAppRequest;
import software.amazon.awssdk.services.amplify.model.UpdateAppResponse;
import software.amazon.awssdk.utils.ImmutableMap;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
    public void tear_down() {
        verify(amplifyClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(amplifyClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        stubProxyClient();
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .name(APP_NAME)
                .customRules(CUSTOM_RULES_CFN)
                .basicAuthConfig(BASIC_AUTH_CONFIG)
                .environmentVariables(ENV_VARS_CFN)
                .autoBranchCreationConfig(AUTO_BRANCH_CREATION_CONFIG)
                .tags(TAGS_CFN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        final ResourceModel expected = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .appName(APP_NAME)
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

        verify(amplifyClient).tagResource(any(TagResourceRequest.class));
        verify(amplifyClient).untagResource(any(UntagResourceRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccess_NoTags() {
        when(proxyClient.client().updateApp(any(UpdateAppRequest.class)))
                .thenReturn(UpdateAppResponse.builder()
                        .app(App.builder()
                                .appArn(APP_ARN)
                                .appId(APP_ID)
                                .name(APP_NAME)
                                .build())
                        .build());
        when(proxyClient.client().getApp(any(GetAppRequest.class)))
                .thenReturn(GetAppResponse.builder()
                        .app(App.builder()
                                .appArn(APP_ARN)
                                .appId(APP_ID)
                                .name(APP_NAME)
                                .build())
                        .build());
        when(proxyClient.client().listTagsForResource(any(ListTagsForResourceRequest.class)))
                .thenReturn(ListTagsForResourceResponse.builder().build());

        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .name(APP_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

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
        verify(amplifyClient, atLeastOnce()).serviceName();
        verify(amplifyClient, never()).tagResource(any(TagResourceRequest.class));
        verify(amplifyClient, never()).untagResource(any(UntagResourceRequest.class));
    }

    private void stubProxyClient() {
        when(proxyClient.client().updateApp(any(UpdateAppRequest.class)))
                .thenReturn(UpdateAppResponse.builder()
                        .app(App.builder()
                                .appArn(APP_ARN)
                                .appId(APP_ID)
                                .name(APP_NAME)
                                .build())
                        .build());
        when(proxyClient.client().getApp(any(GetAppRequest.class)))
                .thenReturn(GetAppResponse.builder()
                        .app(App.builder()
                                .appArn(APP_ARN)
                                .appId(APP_ID)
                                .name(APP_NAME)
                                .tags(Translator.getTagsSDK(TAGS_CFN))
                                .build())
                        .build());
        when(proxyClient.client().listTagsForResource(any(ListTagsForResourceRequest.class)))
                .thenReturn(ListTagsForResourceResponse.builder()
                    .tags(ImmutableMap.of("oldFoo", "oldBar"))
                .build());
        when(proxyClient.client().tagResource(any(TagResourceRequest.class))).thenReturn(TagResourceResponse.builder()
                .build());

        when(proxyClient.client().untagResource(any(UntagResourceRequest.class))).thenReturn(UntagResourceResponse.builder()
                .build());
    }
}
