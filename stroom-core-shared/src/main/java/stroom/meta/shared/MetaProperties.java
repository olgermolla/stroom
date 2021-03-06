package stroom.meta.shared;

public class MetaProperties {
    private Long parentId;
    private String typeName;
    private String feedName;
    private String processorUuid;
    private String pipelineUuid;
    private Long processorTaskId;
    private Long createMs;
    private Long effectiveMs;
    private Long statusMs;

    public Long getParentId() {
        return parentId;
    }

    public String getFeedName() {
        return feedName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getProcessorUuid() {
        return processorUuid;
    }

    public String getPipelineUuid() {
        return pipelineUuid;
    }

    public Long getProcessorTaskId() {
        return processorTaskId;
    }

    public Long getCreateMs() {
        return createMs;
    }

    public Long getEffectiveMs() {
        return effectiveMs;
    }

    public Long getStatusMs() {
        return statusMs;
    }

    public static class Builder {
        private MetaProperties dp = new MetaProperties();

        /**
         * This is a utility method to perform common parent association behaviour, e.g. setting the effective time from the parent.
         *
         * @param parent The parent to set.
         * @return The builder.
         */
        public Builder parent(final Meta parent) {
            // Set effective time from the parent data.
            if (parent != null) {
                dp.parentId = parent.getId();
                if (dp.effectiveMs == null) {
                    if (parent.getEffectiveMs() != null) {
                        dp.effectiveMs = parent.getEffectiveMs();
                    } else {
                        dp.effectiveMs = parent.getCreateMs();
                    }
                }
            } else {
                dp.parentId = null;
            }

            return this;
        }

        public Builder parentId(final Long parentId) {
            dp.parentId = parentId;
            return this;
        }

        public Builder feedName(final String feedName) {
            dp.feedName = feedName;
            return this;
        }

        public Builder typeName(final String typeName) {
            dp.typeName = typeName;
            return this;
        }

        public Builder processorUuid(final String processorUuid) {
            dp.processorUuid = processorUuid;
            return this;
        }

        public Builder pipelineUuid(final String pipelineUuid) {
            dp.pipelineUuid = pipelineUuid;
            return this;
        }

        public Builder processorTaskId(final Long processorTaskId) {
            dp.processorTaskId = processorTaskId;
            return this;
        }

        public Builder createMs(final Long createMs) {
            dp.createMs = createMs;
            return this;
        }

        public Builder effectiveMs(final Long effectiveMs) {
            dp.effectiveMs = effectiveMs;
            return this;
        }

        public Builder statusMs(final Long statusMs) {
            dp.statusMs = statusMs;
            return this;
        }

        public MetaProperties build() {
            final MetaProperties properties = new MetaProperties();
            properties.parentId = dp.parentId;
            properties.typeName = dp.typeName;
            properties.feedName = dp.feedName;
            properties.processorUuid = dp.processorUuid;
            properties.pipelineUuid = dp.pipelineUuid;
            properties.processorTaskId = dp.processorTaskId;
            properties.createMs = dp.createMs;
            properties.effectiveMs = dp.effectiveMs;
            properties.statusMs = dp.statusMs;

            // When were we created
            if (properties.createMs == null) {
                properties.createMs = System.currentTimeMillis();
            }

            // Ensure an effective time
            if (properties.effectiveMs == null) {
                properties.effectiveMs = properties.createMs;
            }

            return properties;
        }
    }
}
