package software.amazon.amplify.branch;

import software.amazon.awssdk.services.amplify.AmplifyClient;
 import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
  public static AmplifyClient getClient() {
    return AmplifyClient.builder()
              .httpClient(LambdaWrapper.HTTP_CLIENT)
              .build();
  }
}
