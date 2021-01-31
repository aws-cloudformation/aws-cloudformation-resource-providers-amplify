package software.amazon.amplify.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;
  protected static String APP_NAME = "dummyName";
  protected static String APP_ID = "dummyId";
  protected static String APP_ARN = String.format("arn:aws:amplify:region:accountId:apps/%s", APP_ID);
  protected static List<String> AUTO_BRANCH_CREATION_PATTERNS = ImmutableList.of("/feature*", "/test*");
  protected static List<EnvironmentVariable> ENV_VARS_CFN = ImmutableList.of(EnvironmentVariable.builder()
          .name("foo")
          .value("bar")
          .build());
  protected static List<CustomRule> CUSTOM_RULES_CFN = ImmutableList.of(
          CustomRule.builder()
            .source("/source")
            .target("/target")
            .status("200")
            .build());
  protected static BasicAuthConfig BASIC_AUTH_CONFIG = BasicAuthConfig.builder()
          .username("dummyUser")
          .password("dummyPass")
          .build();
  protected static AutoBranchCreationConfig AUTO_BRANCH_CREATION_CONFIG = AutoBranchCreationConfig.builder()
          .autoBranchCreationPatterns(ImmutableList.of("/feature*", "/dev*"))
          .build();
//  protected static Map<String, String> TAGS_SDK = ImmutableMap.of("foo", "bar");
  protected static List<Tag> TAGS_CFN = ImmutableList.of(Tag.builder().key("foo").value("bar").build());

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }

  static ProxyClient<AmplifyClient> MOCK_PROXY(
    final AmazonWebServicesClientProxy proxy,
    final AmplifyClient sdkClient) {
    return new ProxyClient<AmplifyClient>() {
      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
      injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
        return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
      CompletableFuture<ResponseT>
      injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
      IterableT
      injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
        return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
      injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
      injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public AmplifyClient client() {
        return sdkClient;
      }
    };
  }
}
