package software.amazon.amplify.common.utils;

import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TagUtils {
    private static void updateTags(final AmazonWebServicesClientProxy proxy,
                            final ProxyClient<AmplifyClient> proxyClient,
                            final ResourceModel model,
                            final Map<String, String> desiredTags) {
        logger.log("INFO: Modifying Tags");
        final Set<Tag> finalTags = convertResourceTagsToSet(desiredTags);
        final Set<Tag> existingTags = getExistingTags(proxy, proxyClient, model);

        final Set<Tag> tagsToRemove = Sets.difference(existingTags, finalTags);
        final Set<Tag> tagsToAdd = Sets.difference(finalTags, existingTags);

        if (tagsToRemove.size() > 0) {
            Collection<String> tagKeys = tagsToRemove.stream().map(Tag::getKey).collect(Collectors.toSet());
            final UntagResourceRequest untagResourceRequest = UntagResourceRequest.builder().resourceArn(model.getArn())
                    .tagKeys(tagKeys).build();
            execute(proxy, untagResourceRequest, proxyClient.client()::untagResource, model, logger);
        }

        if (tagsToAdd.size() > 0) {
            Map<String, String> tags = convertToResourceTags(tagsToAdd);
            final TagResourceRequest tagResourceRequest = TagResourceRequest.builder()
                    .resourceArn(model.getArn()).tags(tags).build();
            execute(proxy, tagResourceRequest, proxyClient.client()::tagResource, model, logger);
        }
        logger.log("INFO: Successfully Updated Tags");
    }

    private static Set<Tag> getExistingTags(final AmazonWebServicesClientProxy proxy,
                                     final ProxyClient<AmplifyClient> proxyClient,
                                     final ResourceModel model) {
        ListTagsForResourceRequest listTagsForResourceRequest = Translator.translateToListTagsForResourceRequest(model.getArn());
        ListTagsForResourceResponse listTagsForResourceResponse = (ListTagsForResourceResponse) execute(proxy,
                listTagsForResourceRequest, proxyClient.client()::listTagsForResource, model, logger);
        return convertResourceTagsToSet(listTagsForResourceResponse.tags());
    }

    private static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        final Set<Tag> tagSet = Sets.newHashSet();
        if (MapUtils.isNotEmpty(resourceTags)) {
            resourceTags.forEach((key, value) -> tagSet.add(Tag.builder().key(key).value(value).build()));
        }
        return tagSet;
    }

    private static Map<String, String> convertToResourceTags(final Collection<Tag> tagSet) {
        final Map<String, String> tagMap = new HashMap<>();
        if (tagSet != null) {
            for (final Tag tag : tagSet) {
                tagMap.put(tag.getKey(), tag.getValue());
            }
        }
        return tagMap;
    }

}
