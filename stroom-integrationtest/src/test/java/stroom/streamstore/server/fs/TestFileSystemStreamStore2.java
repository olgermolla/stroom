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

package stroom.streamstore.server.fs;

import org.hibernate.LazyInitializationException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import stroom.AbstractCoreIntegrationTest;
import stroom.entity.server.util.PeriodUtil;
import stroom.entity.shared.BaseResultList;
import stroom.entity.shared.DocRef;
import stroom.entity.shared.Folder;
import stroom.entity.shared.FolderService;
import stroom.entity.shared.IdRange;
import stroom.entity.shared.PageRequest;
import stroom.entity.shared.Period;
import stroom.feed.shared.Feed;
import stroom.feed.shared.FeedService;
import stroom.streamstore.server.EffectiveMetaDataCriteria;
import stroom.streamstore.server.FindStreamAttributeValueCriteria;
import stroom.streamstore.server.FindStreamVolumeCriteria;
import stroom.streamstore.server.StreamAttributeValueFlush;
import stroom.streamstore.server.StreamAttributeValueService;
import stroom.streamstore.server.StreamException;
import stroom.streamstore.server.StreamMaintenanceService;
import stroom.streamstore.server.StreamSource;
import stroom.streamstore.server.StreamStore;
import stroom.streamstore.server.StreamTarget;
import stroom.streamstore.shared.FindStreamAttributeMapCriteria;
import stroom.streamstore.shared.FindStreamCriteria;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamAttributeConstants;
import stroom.streamstore.shared.StreamAttributeMap;
import stroom.streamstore.shared.StreamAttributeMapService;
import stroom.streamstore.shared.StreamAttributeValue;
import stroom.streamstore.shared.StreamStatus;
import stroom.streamstore.shared.StreamType;
import stroom.streamstore.shared.StreamVolume;
import stroom.streamtask.server.StreamTaskCreator;
import stroom.task.server.TaskMonitorImpl;
import stroom.util.config.StroomProperties;
import stroom.util.date.DateUtil;
import stroom.util.io.StreamUtil;
import stroom.util.test.FileSystemTestUtil;
import stroom.util.test.StroomExpectedException;
import stroom.util.zip.HeaderMap;
import stroom.volume.server.VolumeServiceImpl;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestFileSystemStreamStore2 extends AbstractCoreIntegrationTest {
    private static final int N1 = 1;
    private static final int N13 = 13;
    @Resource
    private FileSystemStreamStore2 metaStore;
    @Resource
    private StreamStore streamStore;
//    @Resource
//    private StreamMaintenanceService streamMaintenanceService;
//    @Resource
//    private StreamAttributeValueService streamAttributeValueService;
//    @Resource
//    private StreamAttributeValueFlush streamAttributeValueFlush;
    @Resource
    private FeedService feedService;
    @Resource
    private FolderService folderService;
//    @Resource
//    private StreamTaskCreator streamTaskCreator;
    @Resource
    private StreamAttributeMapService streamMDService;
    private Feed feed1;
    private Feed feed2;
    private int initialReplicationCount = 1;

    @Override
    protected void onBefore() {
        feed1 = setupFeed("FEED1");
        feed2 = setupFeed("FEED2");
        initialReplicationCount = StroomProperties.getIntProperty(VolumeServiceImpl.PROP_RESILIENT_REPLICATION_COUNT, 1);
        StroomProperties.setIntProperty(VolumeServiceImpl.PROP_RESILIENT_REPLICATION_COUNT, 2, StroomProperties.Source.TEST);
    }

    @Override
    protected void onAfter() {
        StroomProperties.setIntProperty(VolumeServiceImpl.PROP_RESILIENT_REPLICATION_COUNT, initialReplicationCount,
                StroomProperties.Source.TEST);
    }

    /**
     * Setup some test data.
     */
    private Feed setupFeed(final String feedName) {
        Feed sample = feedService.loadByName(feedName);
        if (sample == null) {
            Folder folder = folderService.create(null, FileSystemTestUtil.getUniqueTestString());
            folder = folderService.save(folder);

            sample = feedService.create(DocRef.create(folder), feedName);
            sample.setDescription("Junit");
            sample = feedService.save(sample);
        }
        return sample;
    }

	@Test
	public void testFeedFindAll() throws Exception {
		final FindStreamCriteria findStreamCriteria = new FindStreamCriteria();
//		findStreamCriteria.obtainFeeds().obtainInclude().add(feed1);
//		findStreamCriteria.obtainFeeds().obtainInclude().add(feed2);
		testCriteria(findStreamCriteria, 2);
	}

	@Test
	public void testFeedFindSome() throws Exception {
		final FindStreamCriteria findStreamCriteria = new FindStreamCriteria();
		findStreamCriteria.setPageRequest(new PageRequest(0L, 1));
		findStreamCriteria.obtainFeeds().obtainInclude().add(feed1);
		findStreamCriteria.obtainFeeds().obtainInclude().add(feed2);
		testCriteria(findStreamCriteria, 1);
	}

    @Test
    public void testFeedFindNone() throws Exception {
        final FindStreamCriteria findStreamCriteria = new FindStreamCriteria();
        findStreamCriteria.obtainFeeds().obtainInclude().add(feed1);
        findStreamCriteria.obtainFeeds().obtainExclude().add(feed1);
        testCriteria(findStreamCriteria, 0);
    }

    @Test
    public void testFeedFindOne() throws Exception {
        final FindStreamCriteria findStreamCriteria = new FindStreamCriteria();
        findStreamCriteria.obtainFeeds().obtainExclude().add(feed2);
        testCriteria(findStreamCriteria, 1);
    }

    private void testCriteria(final FindStreamCriteria criteria, final int expectedStreams) throws Exception {
//        streamStore.findDelete(new FindStreamCriteria());

        createStream(feed1, 1L, null);
        createStream(feed2, 1L, null);
//        criteria.obtainStatusSet().add(StreamStatus.UNLOCKED);
        final List<HeaderMap> streams = metaStore.findMeta(criteria);
        Assert.assertEquals(expectedStreams, streams.size());

//        streamStore.findDelete(new FindStreamCriteria());
    }

    private Stream createStream(final Feed feed, final Long streamTaskId, final Long parentStreamId)
            throws IOException {
        final String testString = FileSystemTestUtil.getUniqueTestString();

        Stream stream = Stream.createStream(StreamType.RAW_EVENTS, feed, null);
        stream.setStreamTaskId(streamTaskId);
        stream.setParentStreamId(parentStreamId);

        final StreamTarget streamTarget = streamStore.openStreamTarget(stream);
        streamTarget.getOutputStream().write(testString.getBytes(StreamUtil.DEFAULT_CHARSET));
        streamStore.closeStreamTarget(streamTarget);
        stream = streamTarget.getStream();
        return stream;
    }
}