package software.amazon.amplify.domain;

import java.time.Duration;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.DomainAssociation;
import software.amazon.awssdk.services.amplify.model.DomainStatus;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.GetDomainAssociationResponse;
import software.amazon.awssdk.services.amplify.model.SubDomain;
import software.amazon.awssdk.services.amplify.model.SubDomainSetting;
import software.amazon.awssdk.services.amplify.model.UpdateDomainAssociationRequest;
import software.amazon.awssdk.services.amplify.model.UpdateDomainAssociationResponse;
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
public class UpdateHandlerTest extends AbstractTestBase {

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
    public void tear_down() {
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
        DomainAssociation domainAssociationUpdating = domainAssociationBuilder
                .domainStatus(DomainStatus.UPDATING).build();
        DomainAssociation domainAssociationAvailable = domainAssociationBuilder
                .domainStatus(DomainStatus.AVAILABLE).build();
        when(proxyClient.client().updateDomainAssociation(any(UpdateDomainAssociationRequest.class)))
                .thenReturn(UpdateDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationUpdating)
                        .build());
        when(proxyClient.client().getDomainAssociation(any(GetDomainAssociationRequest.class)))
                .thenReturn(GetDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationUpdating)
                        .build())
                .thenReturn(GetDomainAssociationResponse.builder()
                        .domainAssociation(domainAssociationAvailable)
                        .build());
        final UpdateHandler handler = new UpdateHandler();

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

        final ResourceModel expected = ResourceModel.builder()
                .appId(APP_ID)
                .arn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .subDomainSettings(SUBDOMAIN_SETTINGS_CFN)
                .autoSubDomainCreationPatterns(AUTO_SUBDOMAIN_CREATION_PATTERNS)
                .domainStatus(DomainStatus.AVAILABLE.toString())
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
    public void handleRequest_FailedStatusFailsStabilization() {
        DomainAssociation domainAssociationPending = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.UPDATING)
                .build();
        DomainAssociation domainAssociationFailed = DomainAssociation.builder()
                .domainAssociationArn(DOMAIN_ASSOCIATION_ARN)
                .domainName(DOMAIN_NAME)
                .domainStatus(DomainStatus.FAILED)
                .build();
        UpdateDomainAssociationResponse updateDomainAssociationResponseMock =
                UpdateDomainAssociationResponse.builder().domainAssociation(domainAssociationPending).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociationFailed).build();
        stubProxyClient(updateDomainAssociationResponseMock, getDomainAssociationResponseMock);

        final UpdateHandler handler = new UpdateHandler();
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
        UpdateDomainAssociationResponse updateDomainAssociationResponseMock =
                UpdateDomainAssociationResponse.builder().domainAssociation(domainAssociationPending).build();
        GetDomainAssociationResponse getDomainAssociationResponseMock =
                GetDomainAssociationResponse.builder().domainAssociation(domainAssociationFailed).build();
        stubProxyClient(updateDomainAssociationResponseMock, getDomainAssociationResponseMock);

        final UpdateHandler handler = new UpdateHandler();
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

    private void stubProxyClient(UpdateDomainAssociationResponse updateDomainAssociationResponse,
                                 GetDomainAssociationResponse getDomainAssociationResponse) {
        when(proxyClient.client().updateDomainAssociation(any(UpdateDomainAssociationRequest.class)))
                .thenReturn(updateDomainAssociationResponse);
        when(proxyClient.client().getDomainAssociation(any(GetDomainAssociationRequest.class)))
                .thenReturn(getDomainAssociationResponse);
    }
}
