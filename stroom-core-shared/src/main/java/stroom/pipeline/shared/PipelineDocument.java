/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.pipeline.shared;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import stroom.docstore.shared.Document;
import stroom.pipeline.shared.data.PipelineData;
import stroom.query.api.v2.DocRef;
import stroom.util.shared.HasDisplayValue;
import stroom.util.shared.SharedObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"type", "uuid", "name", "version", "createTime", "updateTime", "createUser", "updateUser", "description", "parentPipeline", "pipelineType", "pipelineData"})
@XmlRootElement(name = "dataReceiptPolicy")
@XmlType(name = "DataReceiptPolicy", propOrder = {"type", "uuid", "name", "version", "createTime", "updateTime", "createUser", "updateUser", "description", "parentPipeline", "pipelineType", "pipelineData"})
public class PipelineDocument extends Document implements SharedObject {
    public static final String DOCUMENT_TYPE = "Pipeline";
    public static final String STEPPING_PERMISSION = "Pipeline Stepping";

    private static final long serialVersionUID = 4519634323788508083L;

    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "parentPipeline")
    private DocRef parentPipeline;
    @XmlElement(name = "pipelineType")
    private String pipelineType = PipelineType.EVENT_DATA.getDisplayValue();
    @XmlElement(name = "pipelineData")
    private PipelineData pipelineData;

    public PipelineDocument() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public DocRef getParentPipeline() {
        return parentPipeline;
    }

    public void setParentPipeline(final DocRef parentPipeline) {
        this.parentPipeline = parentPipeline;
    }

    public String getPipelineType() {
        return pipelineType;
    }

    public void setPipelineType(final String pipelineType) {
        this.pipelineType = pipelineType;
    }

    public PipelineData getPipelineData() {
        return pipelineData;
    }

    public void setPipelineData(final PipelineData pipelineData) {
        this.pipelineData = pipelineData;
    }

    public enum PipelineType implements HasDisplayValue {
        EVENT_DATA("Event Data"), REFERENCE_DATA("Reference Data"), CONTEXT_DATA("Context Data"), REFERENCE_LOADER(
                "Reference Loader"), INDEXING("Indexing"), SEARCH_EXTRACTION("Search Extraction");

        private final String displayValue;

        PipelineType(final String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }
}
