/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.tables;


import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import stroom.index.impl.db.Indexes;
import stroom.index.impl.db.Keys;
import stroom.index.impl.db.Stroom;
import stroom.index.impl.db.tables.records.IndexVolumeGroupLinkRecord;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class IndexVolumeGroupLink extends TableImpl<IndexVolumeGroupLinkRecord> {

    private static final long serialVersionUID = -741705933;

    /**
     * The reference instance of <code>stroom.index_volume_group_link</code>
     */
    public static final IndexVolumeGroupLink INDEX_VOLUME_GROUP_LINK = new IndexVolumeGroupLink();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IndexVolumeGroupLinkRecord> getRecordType() {
        return IndexVolumeGroupLinkRecord.class;
    }

    /**
     * The column <code>stroom.index_volume_group_link.fk_index_volume_group_name</code>.
     */
    public final TableField<IndexVolumeGroupLinkRecord, String> FK_INDEX_VOLUME_GROUP_NAME = createField("fk_index_volume_group_name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.index_volume_group_link.fk_index_volume_id</code>.
     */
    public final TableField<IndexVolumeGroupLinkRecord, Long> FK_INDEX_VOLUME_ID = createField("fk_index_volume_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>stroom.index_volume_group_link</code> table reference
     */
    public IndexVolumeGroupLink() {
        this(DSL.name("index_volume_group_link"), null);
    }

    /**
     * Create an aliased <code>stroom.index_volume_group_link</code> table reference
     */
    public IndexVolumeGroupLink(String alias) {
        this(DSL.name(alias), INDEX_VOLUME_GROUP_LINK);
    }

    /**
     * Create an aliased <code>stroom.index_volume_group_link</code> table reference
     */
    public IndexVolumeGroupLink(Name alias) {
        this(alias, INDEX_VOLUME_GROUP_LINK);
    }

    private IndexVolumeGroupLink(Name alias, Table<IndexVolumeGroupLinkRecord> aliased) {
        this(alias, aliased, null);
    }

    private IndexVolumeGroupLink(Name alias, Table<IndexVolumeGroupLinkRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> IndexVolumeGroupLink(Table<O> child, ForeignKey<O, IndexVolumeGroupLinkRecord> key) {
        super(child, key, INDEX_VOLUME_GROUP_LINK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Stroom.STROOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID, Indexes.INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<IndexVolumeGroupLinkRecord>> getKeys() {
        return Arrays.<UniqueKey<IndexVolumeGroupLinkRecord>>asList(Keys.KEY_INDEX_VOLUME_GROUP_LINK_INDEX_VOLUME_GROUP_LINK_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<IndexVolumeGroupLinkRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<IndexVolumeGroupLinkRecord, ?>>asList(Keys.INDEX_VOLUME_GROUP_LINK_FK_GROUP_NAME, Keys.INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID);
    }

    public IndexVolumeGroup indexVolumeGroup() {
        return new IndexVolumeGroup(this, Keys.INDEX_VOLUME_GROUP_LINK_FK_GROUP_NAME);
    }

    public IndexVolume indexVolume() {
        return new IndexVolume(this, Keys.INDEX_VOLUME_GROUP_LINK_FK_VOLUME_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupLink as(String alias) {
        return new IndexVolumeGroupLink(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupLink as(Name alias) {
        return new IndexVolumeGroupLink(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexVolumeGroupLink rename(String name) {
        return new IndexVolumeGroupLink(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexVolumeGroupLink rename(Name name) {
        return new IndexVolumeGroupLink(name, null);
    }
}
