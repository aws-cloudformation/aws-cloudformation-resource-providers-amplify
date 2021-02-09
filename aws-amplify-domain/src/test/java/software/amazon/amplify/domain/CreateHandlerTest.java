package software.amazon.amplify.domain;

import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.CreateDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.DomainStatus;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.NotFoundException;
import software.amazon.awssdk.services.amplify.model.SubDomain;
import software.amazon.awssdk.services.amplify.model.SubDomainSetting;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
        DomainAssociation.Builder domainAssociationBuilder = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .autoSubDomainCreationPatterns(AUTO_SUBDOMAIN_CREATION_PATTERNS)
                .subDomains(SubDomain.builder()
                        .subDomainSetting(SubDomainSetting.builder()
                                .prefix(PREFIX)
                                .branchName(BRANCH_NAME)
                                .build())
                        .build()
                );

        DomainAssociation domainAssociationInProgress = domainAssociationBuilder
                .domainStatus(DomainStatus.IN_PROGRESS).build();
        DomainAssociation domainAssociationPendingVerification = domainAssociationBuilder
                .domainStatus(DomainStatus.PENDING_VERIFICATION).build();

        when(proxyClient.client().createDomainAssociation(any(CreateDomainAssociationRequest.class)))
                .thenReturn(CreateDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationInProgress)
                        .build());
        when(proxyClient.client().getDomainAssociation(any(GetDomainAssociationRequest.class)))
                .thenThrow(NotFoundException.builder().build())
                .thenReturn(GetDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationInProgress)
                        .build())
                .thenReturn(GetDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationPendingVerification)
                        .build());

        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .domainName(DOMAIN_NAME)
                .subDomainSettings(SUBDOMAIN_SETTINGS_CFN)
                .autoSubDomainCreationPatterns(AUTO_SUBDOMAIN_CREATION_PATTERNS)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        // Verify
        final ResourceModel expected = ResourceModel.builder()
                .appId(APP_ID)
                .arn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .subDomainSettings(SUBDOMAIN_SETTINGS_CFN)
                .autoSubDomainCreationPatterns(AUTO_SUBDOMAIN_CREATION_PATTERNS)
                .domainStatus(DomainStatus.PENDING_VERIFICATION.toString())
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
    public void handleRequest_SimpleSuccess_MinimalDomain() {
        DomainAssociation domainAssociation = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.PENDING_VERIFICATION)
                .build();
        CreateDomainAssociationResponse createDomainAssociationResponseMock =
                CreateDomainAssociationResponse.builder().domainAssociation(domainAssociation).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociation).build();
        stubProxyClient(createDomainAssociationResponseMock, getDomainAssociationResponseMock);

        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .domainName(DOMAIN_NAME)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);
        System.out.println("RESPONSE: " + response);

        // Verify
        final ResourceModel expected = ResourceModel.builder()
                .appId(APP_ID)
                .arn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.PENDING_VERIFICATION.toString())
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
        DomainAssociation domainAssociation =
                DomainAssociation.builder().domainAssociationArn(DOMAIN_ASSOCIATION_ARN).domainName(DOMAIN_NAME).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociation).build();
        when(proxyClient.client().getDomainAssociation(any(GetDomainAssociationRequest.class)))
                .thenReturn(getDomainAssociationResponseMock);
        final ReadHandler handler = new ReadHandler();

        // Model without arn causes CfnNotFoundException
        final ResourceModel modelNoArn = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request1 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelNoArn)
                .build();
        // Verify
        assertThatThrownBy(() -> handler.handleRequest(proxy, request1, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotFoundException.class);

        // Model with arn but without appId or domainName gets appId and domainName populated successfully
        final ResourceModel modelWithArn = ResourceModel.builder().arn(DOMAIN_ASSOCIATION_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request2 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithArn)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request2,
                new CallbackContext(), proxyClient, logger);
        final ResourceModel responseModel = response.getResourceModel();
        // Verify
        assertThat(responseModel.getAppId()).isEqualTo(APP_ID);
        assertThat(responseModel.getDomainName()).isEqualTo(DOMAIN_NAME);
    }

    @Test
    @Tag("SkipCleanup")
    public void handleRequest_InvalidModelProps() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().statusReason("invalid").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class)
                .hasMessageContaining("Attempted to provide value to a read-only property");
    }

    @Test
    public void handleRequest_FailedStatusFailsStabilization() {
        DomainAssociation domainAssociationPending = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.PENDING_VERIFICATION)
                .build();
        DomainAssociation domainAssociationFailed = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.FAILED)
                .build();
        CreateDomainAssociationResponse createDomainAssociationResponseMock =
                CreateDomainAssociationResponse.builder().domainAssociation(domainAssociationPending).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociationFailed).build();
        stubProxyClient(createDomainAssociationResponseMock, getDomainAssociationResponseMock);

        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .domainName(DOMAIN_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        // Verify
        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotStabilizedException.class);
    }

    @Test
    public void handleRequest_UnknownStatusFailsStabilization() {
        DomainAssociation domainAssociationPending = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.PENDING_VERIFICATION)
                .build();
        DomainAssociation domainAssociationFailed = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus("Unknown")
                .build();
        CreateDomainAssociationResponse createDomainAssociationResponseMock =
                CreateDomainAssociationResponse.builder().domainAssociation(domainAssociationPending).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociationFailed).build();
        stubProxyClient(createDomainAssociationResponseMock, getDomainAssociationResponseMock);

        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .appId(APP_ID)
                .domainName(DOMAIN_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        // Verify
        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotStabilizedException.class);
    }

    private void stubProxyClient(CreateDomainAssociationResponse createDomainAssociationResponseMock,
                                                  GetDomainAssociationResponse getDomainAssociationResponseMock) {
        when(proxyClient.client().createDomainAssociation(any(CreateDomainAssociationRequest.class)))
                .thenReturn(createDomainAssociationResponseMock);
        when(proxyClient.client().getDomainAssociation(any(GetDomainAssociationRequest.class)))
                .thenThrow(NotFoundException.builder().build())
                .thenReturn(getDomainAssociationResponseMock);
    }
}
