package software.amazon.amplify.common.utils;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
// Resource Arn Formats:
// App
// arn:aws:amplify:region:account:apps/appId
// Branch
// arn:aws:amplify:region:account:apps/appId/branches/branchName
// Domain
// arn:aws:amplify:region:account:apps/appId/domains/domainName

public class ArnUtils {
    private static final int ARN_SPLIT_LENGTH = 2;
    private static final int APP_ID_APP_SPLIT_INDEX = 1;
    private static final int APP_ID_BRANCH_SPLIT_INDEX = 0;

    // Get appId from branch or domain ARN
    public static String getAppId(String arnString, String splitKey) {
        final String APP_SPLIT_KEY = "apps/";
        final String[] arnSplit = arnString.split(APP_SPLIT_KEY);
        if (arnSplit.length == ARN_SPLIT_LENGTH) {
            return arnSplit[APP_ID_APP_SPLIT_INDEX].split(splitKey)[APP_ID_BRANCH_SPLIT_INDEX];
        } else {
            throw new CfnInvalidRequestException("Invalid arn: " + arnString);
        }
    }

    // Get branchName or domainName from branch or domain ARN
    public static String getResourceName(String arnString, String splitKey) {
        final int BRANCH_IDX = 1;
        final String[] arnSplit = arnString.split(splitKey);
        if (arnSplit.length == ARN_SPLIT_LENGTH) {
            return arnSplit[BRANCH_IDX];
        } else {
            throw new CfnInvalidRequestException("Invalid arn: " + arnString);
        }
    }
}
