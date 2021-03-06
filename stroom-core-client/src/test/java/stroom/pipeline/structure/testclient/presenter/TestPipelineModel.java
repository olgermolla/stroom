/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.pipeline.structure.testclient.presenter;


import org.junit.jupiter.api.Test;
import stroom.docref.DocRef;
import stroom.feed.shared.FeedDoc;
import stroom.pipeline.shared.PipelineDoc;
import stroom.pipeline.shared.PipelineModelException;
import stroom.pipeline.shared.data.PipelineData;
import stroom.pipeline.shared.data.PipelineDataUtil;
import stroom.pipeline.shared.data.PipelineElement;
import stroom.pipeline.shared.data.PipelineElementType;
import stroom.pipeline.shared.data.PipelineElementType.Category;
import stroom.pipeline.shared.data.PipelinePropertyType;
import stroom.pipeline.structure.client.presenter.DefaultPipelineTreeBuilder;
import stroom.pipeline.structure.client.presenter.PipelineModel;
import stroom.data.shared.StreamTypeNames;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestPipelineModel {
    private static final PipelineElementType ELEM_TYPE = new PipelineElementType("TestElement", null,
            new String[]{PipelineElementType.ROLE_TARGET, PipelineElementType.ROLE_HAS_TARGETS}, null);
    private static final PipelinePropertyType PROP_TYPE1 = new PipelinePropertyType.Builder()
            .elementType(ELEM_TYPE)
            .name("TestProperty1")
            .type("String")
            .build();
    private static final PipelinePropertyType PROP_TYPE2 = new PipelinePropertyType.Builder()
            .elementType(ELEM_TYPE)
            .name("TestProperty2")
            .type("String")
            .build();
    private static final PipelinePropertyType PROP_TYPE3 = new PipelinePropertyType.Builder()
            .elementType(ELEM_TYPE)
            .name("TestProperty3")
            .type("String")
            .build();
    private static final PipelinePropertyType PROP_TYPE4 = new PipelinePropertyType.Builder()
            .elementType(ELEM_TYPE)
            .name("TestProperty4")
            .type("String")
            .build();

    @Test
    void testBasic() {
        test(null, null, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void testSimple() {
        final PipelineData pipelineData = new PipelineData();
        pipelineData.addElement(ELEM_TYPE, "test1");
        pipelineData.addElement(ELEM_TYPE, "test2");
        pipelineData.addLink("test1", "test2");

        test(null, pipelineData, 2, 0, 0, 0, 0, 0, 1, 0);
    }

    @Test
    void testComplex() {
        final PipelineData pipelineData = new PipelineData();
        pipelineData.addElement(ELEM_TYPE, "test1");
        pipelineData.addElement(ELEM_TYPE, "test2");
        pipelineData.addElement(ELEM_TYPE, "test3");
        pipelineData.addElement(ELEM_TYPE, "test4");
        pipelineData.addLink("test1", "test2");
        pipelineData.addLink("test2", "test3");
        pipelineData.addLink("test3", "test4");
        pipelineData.removeElement(ELEM_TYPE, "test4");

        test(null, pipelineData, 3, 0, 0, 0, 0, 0, 2, 0);
    }

    @Test
    void testComplexWithProperties() {
        final PipelineData pipelineData = new PipelineData();
        pipelineData.addElement(ELEM_TYPE, "test1");
        pipelineData.addElement(ELEM_TYPE, "test2");
        pipelineData.addElement(ELEM_TYPE, "test3");
        pipelineData.addElement(ELEM_TYPE, "test4");
        pipelineData.addLink("test1", "test2");
        pipelineData.addLink("test2", "test3");
        pipelineData.addLink("test3", "test4");
        pipelineData.addProperty("test1", PROP_TYPE1, true);
        pipelineData.addProperty("test2", PROP_TYPE2, true);
        pipelineData.addProperty("test3", PROP_TYPE3, true);
        pipelineData.addProperty("test4", PROP_TYPE4, true);
        pipelineData.removeElement(ELEM_TYPE, "test4");

        test(null, pipelineData, 3, 0, 3, 0, 0, 0, 2, 0);
    }

    @Test
    void testComplexWithPropRemove() {
        final PipelineData pipelineData = new PipelineData();
        pipelineData.addElement(ELEM_TYPE, "test1");
        pipelineData.addElement(ELEM_TYPE, "test2");
        pipelineData.addElement(ELEM_TYPE, "test3");
        pipelineData.addElement(ELEM_TYPE, "test4");
        pipelineData.addLink("test1", "test2");
        pipelineData.addLink("test2", "test3");
        pipelineData.addLink("test3", "test4");
        pipelineData.addProperty("test1", PROP_TYPE1, true);
        pipelineData.addProperty("test2", PROP_TYPE2, true);
        pipelineData.addProperty("test3", PROP_TYPE3, true);
        pipelineData.addProperty("test4", PROP_TYPE4, true);
        pipelineData.removeProperty("test2", PROP_TYPE2);
        pipelineData.removeElement(ELEM_TYPE, "test4");

        test(null, pipelineData, 3, 0, 2, 1, 0, 0, 2, 0);
    }

    @Test
    void testUnknownElement() {
        final PipelineData pipelineData = new PipelineData();
        pipelineData.addElement(ELEM_TYPE, "test");
        pipelineData.addLink("unknown", "test");

        test(null, pipelineData, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void testInheritanceAdditive() {
        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addElement(ELEM_TYPE, "test3");
        override.addLink("test2", "test3");

        test(baseStack, override, 1, 0, 0, 0, 0, 0, 1, 0);
    }

    @Test
    void testInheritanceRemove() {
        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addElement(ELEM_TYPE, "test3");
        override.removeElement(ELEM_TYPE, "test2");
        override.addLink("test2", "test3");

        test(baseStack, override, 0, 1, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void testInheritancePropertiesSame() {
        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addProperty("test1", PROP_TYPE1, false);
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addProperty("test1", PROP_TYPE1, false);

        test(baseStack, override, 0, 0, 1, 0, 0, 0, 0, 0);
    }

    @Test
    void testInheritancePropertiesDiff() {
        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addProperty("test1", PROP_TYPE1, false);
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addProperty("test1", PROP_TYPE1, true);

        test(baseStack, override, 0, 0, 1, 0, 0, 0, 0, 0);
    }

    @Test
    void testInheritancePropertiesRemove() {
        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addProperty("test1", PROP_TYPE1, false);
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.removeProperty("test1", PROP_TYPE1);

        test(baseStack, override, 0, 0, 0, 1, 0, 0, 0, 0);
    }

    @Test
    void testInheritanceRefsSame() {
        final DocRef pipeline = new DocRef(PipelineDoc.DOCUMENT_TYPE, "1");
        final DocRef feed = new DocRef(FeedDoc.DOCUMENT_TYPE, "1");

        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addPipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.EVENTS));
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addPipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.EVENTS));

        test(baseStack, override, 0, 0, 0, 0, 1, 0, 0, 0);
    }

    @Test
    void testInheritanceRefsDiff() {
        final DocRef pipeline = new DocRef(PipelineDoc.DOCUMENT_TYPE, "1");
        final DocRef feed = new DocRef(FeedDoc.DOCUMENT_TYPE, "1");

        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addPipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.EVENTS));
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.addPipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.REFERENCE));

        test(baseStack, override, 0, 0, 0, 0, 1, 0, 0, 0);
    }

    @Test
    void testInheritanceRefsRemove() {
        final DocRef pipeline = new DocRef(PipelineDoc.DOCUMENT_TYPE, "1");
        final DocRef feed = new DocRef(FeedDoc.DOCUMENT_TYPE, "1");

        final List<PipelineData> baseStack = new ArrayList<>();

        final PipelineData base = new PipelineData();
        base.addElement(ELEM_TYPE, "test1");
        base.addElement(ELEM_TYPE, "test2");
        base.addPipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.EVENTS));
        base.addLink("test1", "test2");
        baseStack.add(base);

        final PipelineData override = new PipelineData();
        override.removePipelineReference(
                PipelineDataUtil.createReference("test1", "testProp", pipeline, feed, StreamTypeNames.EVENTS));

        test(baseStack, override, 0, 0, 0, 0, 0, 1, 0, 0);
    }

    @Test
    void testMove() {
        final DefaultPipelineTreeBuilder builder = new DefaultPipelineTreeBuilder();

        final PipelineElementType sourceElementType = new PipelineElementType("Source", null,
                new String[]{PipelineElementType.ROLE_SOURCE, PipelineElementType.ROLE_HAS_TARGETS, PipelineElementType.VISABILITY_SIMPLE}, null);

        final PipelineElementType combinedParserElementType = new PipelineElementType("CombinedParser", Category.PARSER,
                new String[]{PipelineElementType.ROLE_PARSER,
                        PipelineElementType.ROLE_HAS_TARGETS, PipelineElementType.VISABILITY_SIMPLE,
                        PipelineElementType.VISABILITY_STEPPING, PipelineElementType.ROLE_MUTATOR,
                        PipelineElementType.ROLE_HAS_CODE}, null);

        final PipelineElementType findReplaceElementType = new PipelineElementType("FindReplaceFilter", Category.READER,
                new String[]{PipelineElementType.ROLE_TARGET,
                        PipelineElementType.ROLE_HAS_TARGETS,
                        PipelineElementType.ROLE_READER,
                        PipelineElementType.ROLE_MUTATOR,
                        PipelineElementType.VISABILITY_STEPPING}, null);

        final PipelineData pipelineData = new PipelineData();
//        pipelineData.addElement(sourceElementType, "Source");
        pipelineData.addElement(combinedParserElementType, "combinedParser");
        pipelineData.addElement(findReplaceElementType, "findReplaceFilter");

        pipelineData.addLink("Source", "combinedParser");
        pipelineData.addLink("Source", "findReplaceFilter");

        final PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.setBaseStack(null);
        pipelineModel.setPipelineData(pipelineData);
        pipelineModel.build();
        builder.getTree(pipelineModel);

        pipelineModel.removeElement(new PipelineElement("combinedParser", "CombinedParser"));
        pipelineModel.addExistingElement(new PipelineElement("findReplaceFilter", "FindReplaceFilter"), new PipelineElement("combinedParser", "CombinedParser"));

        pipelineModel.build();
        builder.getTree(pipelineModel);
    }

    private void test(final List<PipelineData> baseStack, final PipelineData pipelineData, final int addedElements,
                      final int removedElements, final int addedProperties, final int removedProperties,
                      final int addedPipelineReferences, final int removedPipelineReferences, final int addedLinks,
                      final int removedLinks) throws PipelineModelException {
        final PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.setBaseStack(baseStack);
        pipelineModel.setPipelineData(pipelineData);
        pipelineModel.build();
        final PipelineData diff = pipelineModel.diff();

        assertThat(diff.getAddedElements().size()).isEqualTo(addedElements);
        assertThat(diff.getRemovedElements().size()).isEqualTo(removedElements);
        assertThat(diff.getAddedProperties().size()).isEqualTo(addedProperties);
        assertThat(diff.getRemovedProperties().size()).isEqualTo(removedProperties);
        assertThat(diff.getAddedPipelineReferences().size()).isEqualTo(addedPipelineReferences);
        assertThat(diff.getRemovedPipelineReferences().size()).isEqualTo(removedPipelineReferences);
        assertThat(diff.getAddedLinks().size()).isEqualTo(addedLinks);
        assertThat(diff.getRemovedLinks().size()).isEqualTo(removedLinks);
    }
}
