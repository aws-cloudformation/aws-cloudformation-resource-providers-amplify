package software.amazon.amplify.app;

import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.amplify.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.amplify.model.TagResourceRequest;
import software.amazon.awssdk.services.amplify.model.UntagResourceRequest;
import software.amazon.awssdk.services.amplify.model.UpdateAppResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<AmplifyClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Amplify-App::Update", proxyClient, model, callbackContext)
                    .translateToServiceRequest(resourceModel -> Translator.translateToUpdateRequest(resourceModel))
                    .makeServiceCall((updateAppRequest, proxyInvocation) -> {
                        UpdateAppResponse response = (UpdateAppResponse) execute(proxy, updateAppRequest,
                                proxyInvocation.client()::updateApp, model, logger);
                        setResourceModelId(model, response.app());
                        return response;
                    })
                    .progress())
            .then(progress -> updateTags(proxy, proxyClient, model, convertToResourceTags(model.getTags()), progress))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(final AmazonWebServicesClientProxy proxy,
                                                                     final ProxyClient<AmplifyClient> proxyClient,
                                                                     final ResourceModel model,
                                                                     final Map<String, String> desiredTags,
                                                                     final ProgressEvent<ResourceModel, CallbackContext> progress) {
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
        return progress;
    }

    private Set<Tag> getExistingTags(final AmazonWebServicesClientProxy proxy,
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
