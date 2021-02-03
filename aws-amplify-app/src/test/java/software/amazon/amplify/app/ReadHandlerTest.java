package software.amazon.amplify.app;

import java.time.Duration;

import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.App;
import software.amazon.awssdk.services.amplify.model.GetAppRequest;
import software.amazon.awssdk.services.amplify.model.GetAppResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .name(APP_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ResourceModel expected = ResourceModel.builder()
                .arn(APP_ARN)
                .appId(APP_ID)
                .appName(APP_NAME)
                .customRules(CUSTOM_RULES_CFN)
                .tags(TAGS_CFN)
                .build();

        when(proxyClient.client().getApp(any(GetAppRequest.class)))
                .thenReturn(GetAppResponse.builder()
                        .app(App.builder()
                                .appArn(APP_ARN)
                                .appId(APP_ID)
                                .name(APP_NAME)
                                .autoBranchCreationPatterns(AUTO_BRANCH_CREATION_PATTERNS)
                                .customRules(Translator.getCustomRulesSDK(CUSTOM_RULES_CFN))
                                .tags(Translator.getTagsSDK(TAGS_CFN))
                                .build())
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expected);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
