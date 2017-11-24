/*
 * Copyright 2017 Crown Copyright
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

package stroom.pipeline.server;

import stroom.pipeline.shared.PipelineDocument;
import stroom.query.api.v2.DocRef;

public final class PipelineTestUtil {
    private static final PipelineMarshaller pipelineMarshaller = new PipelineMarshaller();

    private PipelineTestUtil() {
    }

    public static PipelineDocument createBasicPipeline(final String data) {
        PipelineDocument pipelineDocument = new PipelineDocument();
        pipelineDocument.setName("test");
        pipelineDocument.setDescription("test");
        if (data != null) {
            pipelineDocument.setData(data);
            pipelineDocument = pipelineMarshaller.unmarshal(pipelineDocument);
        }
        return pipelineDocument;
    }


    public static PipelineDocument createTestPipeline(final PipelineDocumentService pipelineDocumentService, final String data) {
        return createTestPipeline(pipelineDocumentService, "test", "test", data);
    }

    public static PipelineDocument createTestPipeline(final PipelineDocumentService pipelineDocumentService, final String name,
                                                    final String description, final String data) {
        PipelineDocument pipelineDocument = pipelineDocumentService.create(name);
        pipelineDocument.setName(name);
        pipelineDocument.setDescription(description);
        if (data != null) {
            pipelineDocument.setData(data);
            pipelineDocument = pipelineMarshaller.unmarshal(pipelineDocument);
        }
        return pipelineDocumentService.save(pipelineDocument);
    }

    public static PipelineDocument loadPipeline(final PipelineDocument pipeline) {
        return pipelineMarshaller.unmarshal(pipeline);
    }

    public static PipelineDocument savePipeline(final PipelineDocument pipeline) {
        return pipelineMarshaller.marshal(pipeline);
    }
}
