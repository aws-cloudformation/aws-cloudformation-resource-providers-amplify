package software.amazon.amplify.common.utils;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

public class ArnUtils {
    private static final int ARN_SPLIT_LENGTH = 2;
    private static final int APP_ID_APP_SPLIT_INDEX = 1;
    private static final int APP_ID_BRANCH_SPLIT_INDEX = 0;

    public static String getAppId(String arnString, String splitKey) {
        final String APP_SPLIT_KEY = "apps/";
        final String[] arnSplit = arnString.split(APP_SPLIT_KEY);
        if (arnSplit.length == ARN_SPLIT_LENGTH) {
            return arnSplit[APP_ID_APP_SPLIT_INDEX].split(splitKey)[APP_ID_BRANCH_SPLIT_INDEX];
        } else {
            throw new CfnInvalidRequestException("Invalid arn: " + arnString);
        }
    }
}
