/*
 * This file is generated by jOOQ.
 */
package stroom.security.impl.db.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import stroom.security.impl.db.Indexes;
import stroom.security.impl.db.Keys;
import stroom.security.impl.db.Stroom;
import stroom.security.impl.db.tables.records.StroomUserGroupsRecord;


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
public class StroomUserGroups extends TableImpl<StroomUserGroupsRecord> {

    private static final long serialVersionUID = -222604974;

    /**
     * The reference instance of <code>stroom.stroom_user_groups</code>
     */
    public static final StroomUserGroups STROOM_USER_GROUPS = new StroomUserGroups();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<StroomUserGroupsRecord> getRecordType() {
        return StroomUserGroupsRecord.class;
    }

    /**
     * The column <code>stroom.stroom_user_groups.id</code>.
     */
    public final TableField<StroomUserGroupsRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>stroom.stroom_user_groups.user_uuid</code>.
     */
    public final TableField<StroomUserGroupsRecord, String> USER_UUID = createField("user_uuid", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.stroom_user_groups.group_uuid</code>.
     */
    public final TableField<StroomUserGroupsRecord, String> GROUP_UUID = createField("group_uuid", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * Create a <code>stroom.stroom_user_groups</code> table reference
     */
    public StroomUserGroups() {
        this(DSL.name("stroom_user_groups"), null);
    }

    /**
     * Create an aliased <code>stroom.stroom_user_groups</code> table reference
     */
    public StroomUserGroups(String alias) {
        this(DSL.name(alias), STROOM_USER_GROUPS);
    }

    /**
     * Create an aliased <code>stroom.stroom_user_groups</code> table reference
     */
    public StroomUserGroups(Name alias) {
        this(alias, STROOM_USER_GROUPS);
    }

    private StroomUserGroups(Name alias, Table<StroomUserGroupsRecord> aliased) {
        this(alias, aliased, null);
    }

    private StroomUserGroups(Name alias, Table<StroomUserGroupsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> StroomUserGroups(Table<O> child, ForeignKey<O, StroomUserGroupsRecord> key) {
        super(child, key, STROOM_USER_GROUPS);
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
        return Arrays.<Index>asList(Indexes.STROOM_USER_GROUPS_GROUP_UUID, Indexes.STROOM_USER_GROUPS_PRIMARY, Indexes.STROOM_USER_GROUPS_USER_UUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<StroomUserGroupsRecord, Long> getIdentity() {
        return Keys.IDENTITY_STROOM_USER_GROUPS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<StroomUserGroupsRecord> getPrimaryKey() {
        return Keys.KEY_STROOM_USER_GROUPS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<StroomUserGroupsRecord>> getKeys() {
        return Arrays.<UniqueKey<StroomUserGroupsRecord>>asList(Keys.KEY_STROOM_USER_GROUPS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<StroomUserGroupsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<StroomUserGroupsRecord, ?>>asList(Keys.STROOM_USER_GROUPS_IBFK_1, Keys.STROOM_USER_GROUPS_IBFK_2);
    }

    public StroomUser stroomUserGroupsIbfk_1() {
        return new StroomUser(this, Keys.STROOM_USER_GROUPS_IBFK_1);
    }

    public StroomUser stroomUserGroupsIbfk_2() {
        return new StroomUser(this, Keys.STROOM_USER_GROUPS_IBFK_2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StroomUserGroups as(String alias) {
        return new StroomUserGroups(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StroomUserGroups as(Name alias) {
        return new StroomUserGroups(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public StroomUserGroups rename(String name) {
        return new StroomUserGroups(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public StroomUserGroups rename(Name name) {
        return new StroomUserGroups(name, null);
    }
}
