package knn.internal;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import knn.api.KnowledgeNode;
import tags.Tag;

import java.util.Map;
import java.util.Set;

interface DirectSearcherFactory {
    @Inject
    DirectSearcher create(
            @Assisted("mapKN") Map<Tag, KnowledgeNode> mapKN,
            @Assisted("activeTags") Set<Tag> activeTags);
}