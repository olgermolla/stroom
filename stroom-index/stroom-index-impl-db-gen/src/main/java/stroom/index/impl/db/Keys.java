/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db;


import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;
import stroom.index.impl.db.tables.IndexShard;
import stroom.index.impl.db.tables.IndexVolume;
import stroom.index.impl.db.tables.IndexVolumeGroup;
import stroom.index.impl.db.tables.IndexVolumeGroupLink;
import stroom.index.impl.db.tables.records.IndexShardRecord;
import stroom.index.impl.db.tables.records.IndexVolumeGroupLinkRecord;
import stroom.index.impl.db.tables.records.IndexVolumeGroupRecord;
import stroom.index.impl.db.tables.records.IndexVolumeRecord;

import javax.annotation.Generated;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>stroom</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<IndexShardRecord, Long> IDENTITY_INDEX_SHARD = Identities0.IDENTITY_INDEX_SHARD;
    public static final Identity<IndexVolumeRecord, Long> IDENTITY_INDEX_VOLUME = Identities0.IDENTITY_INDEX_VOLUME;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<IndexShardRecord> KEY_INDEX_SHARD_PRIMARY = UniqueKeys0.KEY_INDEX_SHARD_PRIMARY;
    public static final UniqueKey<IndexVolumeRecord> KEY_INDEX_VOLUME_PRIMARY = UniqueKeys0.KEY_INDEX_VOLUME_PRIMARY;
    public static final UniqueKey<IndexVolumeRecord> KEY_INDEX_VOLUME_NODE_NAME_PATH = UniqueKeys0.KEY_INDEX_VOLUME_NODE_NAME_PATH;
    public static final UniqueKey<IndexVolumeGroupRecord> KEY_INDEX_VOLUME_GROUP_PRIMARY = UniqueKeys0.KEY_INDEX_VOLUME_GROUP_PRIMARY;
    public static final UniqueKey<IndexVolumeGroupRecord> KEY_INDEX_VOLUME_GROUP_INDEX_VOLUME_GROUP_NAME = UniqueKeys0.KEY_INDEX_VOLUME_GROUP_INDEX_VOLUME_GROUP_NAME;
    public static final UniqueKey<IndexVolumeGroupLinkRecord> KEY_INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_UNIQUE = UniqueKeys0.KEY_INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_UNIQUE;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<IndexShardRecord, IndexVolumeRecord> INDEX_SHARD_FK_VOLUME_ID = ForeignKeys0.INDEX_SHARD_FK_VOLUME_ID;
    public static final ForeignKey<IndexVolumeGroupLinkRecord, IndexVolumeGroupRecord> INDEX_VOLUME_GROUP_LINK_FK_GROUP_NAME = ForeignKeys0.INDEX_VOLUME_GROUP_LINK_FK_GROUP_NAME;
    public static final ForeignKey<IndexVolumeGroupLinkRecord, IndexVolumeRecord> INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID = ForeignKeys0.INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<IndexShardRecord, Long> IDENTITY_INDEX_SHARD = Internal.createIdentity(IndexShard.INDEX_SHARD, IndexShard.INDEX_SHARD.ID);
        public static Identity<IndexVolumeRecord, Long> IDENTITY_INDEX_VOLUME = Internal.createIdentity(IndexVolume.INDEX_VOLUME, IndexVolume.INDEX_VOLUME.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<IndexShardRecord> KEY_INDEX_SHARD_PRIMARY = Internal.createUniqueKey(IndexShard.INDEX_SHARD, "KEY_index_shard_PRIMARY", IndexShard.INDEX_SHARD.ID);
        public static final UniqueKey<IndexVolumeRecord> KEY_INDEX_VOLUME_PRIMARY = Internal.createUniqueKey(IndexVolume.INDEX_VOLUME, "KEY_index_volume_PRIMARY", IndexVolume.INDEX_VOLUME.ID);
        public static final UniqueKey<IndexVolumeRecord> KEY_INDEX_VOLUME_NODE_NAME_PATH = Internal.createUniqueKey(IndexVolume.INDEX_VOLUME, "KEY_index_volume_node_name_path", IndexVolume.INDEX_VOLUME.NODE_NAME, IndexVolume.INDEX_VOLUME.PATH);
        public static final UniqueKey<IndexVolumeGroupRecord> KEY_INDEX_VOLUME_GROUP_PRIMARY = Internal.createUniqueKey(IndexVolumeGroup.INDEX_VOLUME_GROUP, "KEY_index_volume_group_PRIMARY", IndexVolumeGroup.INDEX_VOLUME_GROUP.NAME);
        public static final UniqueKey<IndexVolumeGroupRecord> KEY_INDEX_VOLUME_GROUP_INDEX_VOLUME_GROUP_NAME = Internal.createUniqueKey(IndexVolumeGroup.INDEX_VOLUME_GROUP, "KEY_index_volume_group_index_volume_group_name", IndexVolumeGroup.INDEX_VOLUME_GROUP.NAME);
        public static final UniqueKey<IndexVolumeGroupLinkRecord> KEY_INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_UNIQUE = Internal.createUniqueKey(IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK, "KEY_index_volume_group_link_index_volume_group_link_unique", IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK.FK_INDEX_VOLUME_GROUP_NAME, IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK.FK_INDEX_VOLUME_ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<IndexShardRecord, IndexVolumeRecord> INDEX_SHARD_FK_VOLUME_ID = Internal.createForeignKey(stroom.index.impl.db.Keys.KEY_INDEX_VOLUME_PRIMARY, IndexShard.INDEX_SHARD, "index_shard_fk_volume_id", IndexShard.INDEX_SHARD.FK_VOLUME_ID);
        public static final ForeignKey<IndexVolumeGroupLinkRecord, IndexVolumeGroupRecord> INDEX_VOLUME_GROUP_LINK_FK_GROUP_NAME = Internal.createForeignKey(stroom.index.impl.db.Keys.KEY_INDEX_VOLUME_GROUP_PRIMARY, IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK, "index_volume_group_link_fk_group_name", IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK.FK_INDEX_VOLUME_GROUP_NAME);
        public static final ForeignKey<IndexVolumeGroupLinkRecord, IndexVolumeRecord> INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID = Internal.createForeignKey(stroom.index.impl.db.Keys.KEY_INDEX_VOLUME_PRIMARY, IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK, "index_volume_group_link_fk_volume_id", IndexVolumeGroupLink.INDEX_VOLUME_GROUP_LINK.FK_INDEX_VOLUME_ID);
    }
}
