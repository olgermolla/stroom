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

package stroom.security.impl;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.security.shared.User;
import stroom.security.shared.UserAppPermissions;
import stroom.test.common.util.test.FileSystemTestUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TestAppPermissionServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAppPermissionServiceImpl.class);

    private static UserService userService;
    private static UserAppPermissionService userAppPermissionService;
    private static UserGroupsCache userGroupsCache;
    private static UserAppPermissionsCache userAppPermissionsCache;

    @BeforeAll
    static void beforeAll() {
        final Injector injector = Guice.createInjector(new TestModule());

        userService = injector.getInstance(UserService.class);
        userAppPermissionService = injector.getInstance(UserAppPermissionService.class);
        userGroupsCache = injector.getInstance(UserGroupsCache.class);
        userAppPermissionsCache = injector.getInstance(UserAppPermissionsCache.class);
    }

    @Test
    void test() {
        final User userGroup1 = createUserGroup(String.format("Group_1_%s", UUID.randomUUID()));
        final User userGroup2 = createUserGroup(String.format("Group_2_%s", UUID.randomUUID()));
        final User userGroup3 = createUserGroup(String.format("Group_3_%s", UUID.randomUUID()));

        final String c1 = "C1";
        final String p1 = "P1";
        final String p2 = "P2";

        addPermissions(userGroup1, c1, p1);
        addPermissions(userGroup2, c1, p2);
        addPermissions(userGroup3, c1);

        checkPermissions(userGroup1, c1, p1);
        checkPermissions(userGroup2, c1, p2);
        checkPermissions(userGroup3, c1);

        removePermissions(userGroup2, p2);
        checkPermissions(userGroup2, c1);

        // Check user permissions.
        final User user = createUser(FileSystemTestUtil.getUniqueTestString());
        userService.addUserToGroup(user.getUuid(), userGroup1.getUuid());
        userService.addUserToGroup(user.getUuid(), userGroup3.getUuid());
        checkUserPermissions(user, c1, p1);

        addPermissions(userGroup2, c1, p2);

        userService.addUserToGroup(user.getUuid(), userGroup2.getUuid());
        checkUserPermissions(user, c1, p1, p2);

        removePermissions(userGroup2, p2);
        checkUserPermissions(user, c1, p1);
    }

    private void addPermissions(final User user, final String... permissions) {
        for (final String permission : permissions) {
            try {
                userAppPermissionService.addPermission(user.getUuid(), permission);
            } catch (final Exception e) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    private void removePermissions(final User user, final String... permissions) {
        for (final String permission : permissions) {
            userAppPermissionService.removePermission(user.getUuid(), permission);
        }
    }

    private void checkPermissions(final User user, final String... permissions) {
        final UserAppPermissions userAppPermissions = userAppPermissionService
                .getPermissionsForUser(user);
        final Set<String> permissionSet = userAppPermissions.getUserPermissons();
        assertThat(permissionSet.size()).isEqualTo(permissions.length);
        for (final String permission : permissions) {
            assertThat(permissionSet.contains(permission)).isTrue();
        }

        checkUserPermissions(user, permissions);
    }

    private void checkUserPermissions(final User user, final String... permissions) {
        final Set<User> allUsers = new HashSet<>();
        allUsers.add(user);
        allUsers.addAll(userService.findGroupsForUser(user.getUuid()));

        final Set<String> combinedPermissions = new HashSet<>();
        for (final User userRef : allUsers) {
            final UserAppPermissions userAppPermissions = userAppPermissionService.getPermissionsForUser(userRef);
            final Set<String> userPermissions = userAppPermissions.getUserPermissons();
            combinedPermissions.addAll(userPermissions);
        }

        assertThat(combinedPermissions.size()).isEqualTo(permissions.length);
        for (final String permission : permissions) {
            assertThat(combinedPermissions.contains(permission)).isTrue();
        }

        checkUserCachePermissions(user, permissions);
    }

    private void checkUserCachePermissions(final User user, final String... permissions) {
        userGroupsCache.clear();
        userAppPermissionsCache.clear();

        final Set<User> allUsers = new HashSet<>();
        allUsers.add(user);
        allUsers.addAll(userGroupsCache.get(user.getUuid()));

        final Set<String> combinedPermissions = new HashSet<>();
        for (final User userRef : allUsers) {
            final UserAppPermissions userAppPermissions = userAppPermissionsCache.get(userRef);
            final Set<String> userPermissions = userAppPermissions.getUserPermissons();
            combinedPermissions.addAll(userPermissions);
        }

        assertThat(combinedPermissions.size()).isEqualTo(permissions.length);
        for (final String permission : permissions) {
            assertThat(combinedPermissions.contains(permission)).isTrue();
        }
    }

    private User createUser(final String name) {
        final User userRef = userService.createUser(name);
        assertThat(userRef).isNotNull();
        final User user = userService.loadByUuid(userRef.getUuid());
        assertThat(user).isNotNull();
        return user;
    }

    private User createUserGroup(final String name) {
        final User userRef = userService.createUserGroup(name);
        assertThat(userRef).isNotNull();
        final User user = userService.loadByUuid(userRef.getUuid());
        assertThat(user).isNotNull();
        return user;
    }
}
