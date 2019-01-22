package stroom.security.impl.db;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import stroom.security.dao.AppPermissionDao;
import static stroom.security.impl.db.tables.AppPermission.APP_PERMISSION;
import static stroom.security.impl.db.tables.StroomUser.STROOM_USER;

import stroom.security.impl.db.tables.records.AppPermissionRecord;
import stroom.util.jooq.JooqUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.Set;

public class AppPermissionDaoImpl implements AppPermissionDao {

    private final ConnectionProvider connectionProvider;

    @Inject
    public AppPermissionDaoImpl(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Set<String> getPermissionsForUser(final String userUuid) {
        return JooqUtil.contextResult(connectionProvider, context ->
                context.select()
                        .from(APP_PERMISSION)
                        .where(APP_PERMISSION.USER_UUID.eq(userUuid))
                        .fetchSet(APP_PERMISSION.PERMISSION));
    }

    @Override
    public void addPermission(final String userUuid, final String permission) {
        JooqUtil.context(connectionProvider, context -> {
            final Record user = context.select().from(STROOM_USER).where(STROOM_USER.UUID.eq(userUuid)).fetchOne();
            if (null == user) {
                throw new SecurityException(String.format("Could not find user: %s", userUuid));
            }

            final AppPermissionRecord r = context.newRecord(APP_PERMISSION);
            r.setUserUuid(userUuid);
            r.setPermission(permission);
            r.store();
        });
    }

    @Override
    public void removePermission(final String userUuid, String permission) {
        JooqUtil.context(connectionProvider, context ->
                context.deleteFrom(APP_PERMISSION)
                        .where(APP_PERMISSION.USER_UUID.eq(userUuid))
                        .and(APP_PERMISSION.PERMISSION.eq(permission)).execute());
    }
}
