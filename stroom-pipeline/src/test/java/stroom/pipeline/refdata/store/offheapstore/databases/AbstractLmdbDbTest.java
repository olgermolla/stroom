/*
 * Copyright 2018 Crown Copyright
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
 *
 */

package stroom.pipeline.refdata.store.offheapstore.databases;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lmdbjava.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.util.ByteSizeUnit;
import stroom.util.io.FileUtil;
import stroom.test.common.util.test.StroomUnitTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractLmdbDbTest extends StroomUnitTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLmdbDbTest.class);
    private static final long DB_MAX_SIZE = ByteSizeUnit.KIBIBYTE.longBytes(1000);
    protected Env<ByteBuffer> lmdbEnv = null;
    private Path dbDir = null;

    @BeforeEach
    protected void setup() throws IOException {
        dbDir = Files.createTempDirectory("stroom");
        LOGGER.debug("Creating LMDB environment with maxSize: {}, dbDir {}",
                getMaxSizeBytes(), dbDir.toAbsolutePath().toString());

        lmdbEnv = Env.create()
                .setMapSize(getMaxSizeBytes())
                .setMaxDbs(10)
                .open(dbDir.toFile());
    }

    @AfterEach
    public void teardown() throws IOException {
        if (lmdbEnv != null) {
            lmdbEnv.close();
        }
        lmdbEnv = null;
        if (Files.isDirectory(dbDir)) {
            FileUtil.deleteDir(dbDir);
        }
    }

    protected Path getDbDir() {
        return dbDir;
    }

    protected long getMaxSizeBytes() {
        return DB_MAX_SIZE;
    }
}
