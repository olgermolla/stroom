/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

import stroom.index.impl.db.tables.IndexVolumeGroup;


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
public class IndexVolumeGroupRecord extends UpdatableRecordImpl<IndexVolumeGroupRecord> implements Record3<String, Long, String> {

    private static final long serialVersionUID = 888252047;

    /**
     * Setter for <code>stroom.index_volume_group.name</code>.
     */
    public void setName(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>stroom.index_volume_group.name</code>.
     */
    public String getName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>stroom.index_volume_group.create_time_ms</code>.
     */
    public void setCreateTimeMs(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>stroom.index_volume_group.create_time_ms</code>.
     */
    public Long getCreateTimeMs() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>stroom.index_volume_group.create_user</code>.
     */
    public void setCreateUser(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>stroom.index_volume_group.create_user</code>.
     */
    public String getCreateUser() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, Long, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, Long, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return IndexVolumeGroup.INDEX_VOLUME_GROUP.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return IndexVolumeGroup.INDEX_VOLUME_GROUP.CREATE_TIME_MS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return IndexVolumeGroup.INDEX_VOLUME_GROUP.CREATE_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getCreateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getCreateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getCreateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getCreateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupRecord value1(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupRecord value2(Long value) {
        setCreateTimeMs(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupRecord value3(String value) {
        setCreateUser(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolumeGroupRecord values(String value1, Long value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached IndexVolumeGroupRecord
     */
    public IndexVolumeGroupRecord() {
        super(IndexVolumeGroup.INDEX_VOLUME_GROUP);
    }

    /**
     * Create a detached, initialised IndexVolumeGroupRecord
     */
    public IndexVolumeGroupRecord(String name, Long createTimeMs, String createUser) {
        super(IndexVolumeGroup.INDEX_VOLUME_GROUP);

        set(0, name);
        set(1, createTimeMs);
        set(2, createUser);
    }
}
