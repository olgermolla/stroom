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

package stroom.pipeline.state;

import stroom.util.pipeline.scope.PipelineScoped;

import java.util.concurrent.atomic.AtomicLong;

@PipelineScoped
public class RecordCount {
    private final AtomicLong readCount = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();
    private volatile long startMs;

    public Incrementor getReadIncrementor() {
        return readCount::incrementAndGet;
    }

    public Incrementor getWriteIncrementor() {
        return writeCount::incrementAndGet;
    }

    public long getRead() {
        return readCount.get();
    }

    public long getWritten() {
        return writeCount.get();
    }

    public long getDuration() {
        return System.currentTimeMillis() - startMs;
    }

    public void setStartMs(final long startMs) {
        this.startMs = startMs;
    }
}
