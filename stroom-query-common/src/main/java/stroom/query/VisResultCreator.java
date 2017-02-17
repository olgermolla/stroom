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

package stroom.query;

import stroom.dashboard.expression.FieldIndexMap;
import stroom.dashboard.expression.Generator;
import stroom.dashboard.expression.TypeConverter;
import stroom.query.api.Field;
import stroom.query.api.Format.Type;
import stroom.query.api.OffsetRange;
import stroom.query.api.Result;
import stroom.query.api.ResultRequest;
import stroom.query.api.TableSettings;
import stroom.query.api.VisResult;
import stroom.query.format.FieldFormatter;
import stroom.util.shared.HasTerminate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisResultCreator implements ResultCreator, HasTerminate {
    private final FieldFormatter fieldFormatter;
    private final Mapper[] mappers;
    private final Field[] fields;
//    private final Field[] valueFields;

    private String error;

    public VisResultCreator(final ResultRequest resultRequest, final Map<String, String> paramMap, final FieldFormatter fieldFormatter) {
        this.fieldFormatter = fieldFormatter;

        final TableSettings[] tableSettings = resultRequest.getTableSettings();

        if (tableSettings.length > 1) {
            mappers = new Mapper[tableSettings.length - 1];
            for (int i = 0; i < tableSettings.length - 1; i++) {
                final TableSettings parent = tableSettings[i];
                final TableSettings child = tableSettings[i + 1];
                mappers[i] = new Mapper(parent, child, paramMap);
            }
        } else {
            mappers = new Mapper[0];
        }

        final TableSettings child = tableSettings[tableSettings.length - 1];
        fields = child.getFields();

//        tfields = Structure = createFieldStructure(fields);
//
//        // Get detail/value fields.
//        valueFields = fieldStructure[fieldStructure.length - 1];
    }

    private Object[] toNodeKey(final Field[] fields, final Key key) {
        if (key == null) {
            return null;
        }

        final List<Object[]> list = new ArrayList<>();
        Key k = key;
        int size = 0;
        while (k != null && k.getValues() != null) {
            list.add(0, k.getValues());
            size += k.getValues().length;
            k = k.getParent();
        }

        final Object[] result = new Object[size];
        int index = 0;
        int i = 0;
        for (final Object[] values : list) {
            final Field field = fields[i++];
            if (values.length == 1) {
                result[index++] = convert(field, values[0]);
            } else {
                for (final Object obj : values) {
                    result[index++] = obj;
                }
            }
        }

        return result;
    }

//    private Field[][] createFieldStructure(final Field[] fields) {
//        int maxGroup = -1;
//        for (final Field field : fields) {
//            Integer group = field.getGroup();
//            if (group != null) {
//                maxGroup = Math.max(maxGroup, group);
//            }
//        }
//        maxGroup++;
//
//        final Map<Integer, List<Field>> map = new HashMap<>();
//        for (final Field field : fields) {
//            Integer group = field.getGroup();
//            if (group == null || group < 0) {
//                group = maxGroup;
//            }
//            final List<Field> fieldList = map.computeIfAbsent(group, k -> new ArrayList<>());
//            fieldList.add(field);
//        }
//
//        final Field[][] fieldStructure = new Field[map.size()][];
//        for (final Entry<Integer, List<Field>> entry : map.entrySet()) {
//            final Integer depth = entry.getKey();
//            final List<Field> fieldList = entry.getValue();
//            fieldList.sort((f1, f2) -> {
//                if (f1.getGroup() == f2.getGroup()) {
//                    return 0;
//                }
//                if (f1.getGroup() == null) {
//                    return -1;
//                } else if (f2.getGroup() == null) {
//                    return 1;
//                }
//                return f1.getGroup().compareTo(f2.getGroup());
//            });
//            fieldStructure[depth] = fieldList.toArray(new Field[fieldList.size()]);
//        }
//
//        return fieldStructure;
//    }

    @Override
    public Result create(final Data data, final ResultRequest resultRequest) {
        if (error == null) {
            try {
                // Map data.
                Data mappedData = data;
                for (final Mapper mapper : mappers) {
                    mappedData = mapper.map(mappedData);
                }

//                final NodeBuilder nodeBuilder = new NodeBuilder(valueFields.length);
                long totalResults = 0;

                // Get top level items.
                final Items<Item> items = mappedData.getChildMap().get(null);

                final List<Object[]> results = new ArrayList<>();

                if (items != null) {
                    final RangeChecker rangeChecker = RangeCheckerFactory.create(resultRequest.getRequestedRange());
                    final OpenGroups openGroups = OpenGroupsFactory.create(resultRequest.getOpenGroups());
                    totalResults = addResults(mappedData, rangeChecker, openGroups, items, results, 0,
                            0);
                }

                final Object[][] values = results.toArray(new Object[results.size()][]);

                return new VisResult(resultRequest.getComponentId(), fields, values, totalResults, error);

            } catch (final Exception e) {
                error = e.getMessage();
            }
        }

        return new VisResult(error);
    }

    private int addResults(final Data data, final RangeChecker rangeChecker,
                           final OpenGroups openGroups, final Items<Item> items, final List<Object[]> results,
                           final int depth, final int parentCount) {
        int count = parentCount;

        for (final Item item : items) {
            if (rangeChecker.check(count)) {
                // TODO : Add hidden fields for parent key, key and depth.
                final Object[] values = new Object[fields.length + 3];

                if (item.getKey() != null) {
                    values[0] = toNodeKey(fields, item.getKey().getParent());
                    values[1] = toNodeKey(fields, item.getKey());
                }
                values[2] = depth;

                // Convert all list into fully resolved objects evaluating
                // functions where necessary.
                for (int i = 0; i < fields.length; i++) {
                    final Object o = item.getValues()[i];
                    if (o != null) {
                        // Convert all list into fully resolved
                        // objects evaluating functions where necessary.
                        Object val = o;
                        if (o instanceof Generator) {
                            final Generator generator = (Generator) o;
                            val = generator.eval();
                        }

                        if (val != null) {
                            if (fieldFormatter != null) {
                                final Field field = fields[i];
                                val = fieldFormatter.format(field, val);
                            } else {
                                final Field field = fields[i];
                                val = convert(field, val);
                            }
                            values[i + 3] = val;
                        }
                    }
                }

                // Add the values.
                results.add(values);

                // Add child results if a node is open.
                if (openGroups.isOpen(item.getKey())) {
                    final Items<Item> childItems = data.getChildMap().get(item.getKey());
                    if (childItems != null) {
                        count = addResults(data, rangeChecker, openGroups,
                                childItems, results, depth + 1, count);
                    }
                }
            }

            // Increment the position.
            count++;
        }

        return count;
    }

    // TODO : Replace this with conversion a the item level.
    private Object convert(final Field field, final Object val) {
        if (field != null && field.getFormat() != null && field.getFormat().getType() != null) {
            final Type type = field.getFormat().getType();
            if (Type.NUMBER.equals(type) || Type.DATE_TIME.equals(type)) {
                return TypeConverter.getDouble(val);
            }
        }

        return val;
    }

    private String[] getTypes(final Field[] fields) {
        String[] types = null;
        if (fields != null) {
            types = new String[fields.length];
            for (int i = 0; i < types.length; i++) {
                final Field field = fields[i];
                final Type type = getType(field);
                types[i] = type.name();
            }
        }
        return types;
    }

    private Type getType(final Field field) {
        Type type = Type.GENERAL;
        if (field != null && field.getFormat() != null && field.getFormat().getType() != null) {
            type = field.getFormat().getType();
        }

        return type;
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @FunctionalInterface
    private interface RangeChecker {
        boolean check(long count);
    }

    @FunctionalInterface
    private interface OpenGroups {
        boolean isOpen(Key group);
    }

    private static class Mapper implements HasTerminate {
        private final String[] parentFields;
        private final FieldIndexMap fieldIndexMap;
        private final TableCoprocessor tableCoprocessor;
        private final TablePayloadHandler tablePayloadHandler;

        public Mapper(final TableSettings parent, final TableSettings child, final Map<String, String> paramMap) {
            parentFields = new String[parent.getFields().length];
            int i = 0;
            for (final Field field : parent.getFields()) {
                parentFields[i++] = field.getName();
            }

            fieldIndexMap = new FieldIndexMap(true);

            final TableCoprocessorSettings tableCoprocessorSettings = new TableCoprocessorSettings(child);
            tableCoprocessor = new TableCoprocessor(tableCoprocessorSettings, fieldIndexMap, this, paramMap);

            final TrimSettings trimSettings = new TrimSettings(child.getMaxResults(), new Integer[]{Integer.MAX_VALUE});
            tablePayloadHandler = new TablePayloadHandler(child.getFields(), true, trimSettings);
        }

        public Data map(final Data data) {
            // Get top level items.

            // TODO : Add an option to get detail level items rather than root level items.
            final Items<Item> items = data.getChildMap().get(null);

            for (final Item item : items) {
                final Object[] values = item.getValues();
                final String[] strings = new String[fieldIndexMap.size()];
                for (int i = 0; i < values.length; i++) {
                    final Object value = values[i];
                    if (value != null) {
                        final int index = fieldIndexMap.get(parentFields[i]);
                        if (index >= 0) {
                            if (value instanceof Generator) {
                                final Generator generator = (Generator) value;
                                strings[index] = TypeConverter.getString(generator.eval());
                            } else {
                                strings[index] = TypeConverter.getString(value);
                            }
                        }
                    }
                }

                // TODO : Receive Object[] for lazy + nested evaluation.
                tableCoprocessor.receive(strings);
            }

            final TablePayload payload = (TablePayload) tableCoprocessor.createPayload();

            tablePayloadHandler.clear();
            tablePayloadHandler.addQueue(payload.getQueue(), this);
            return tablePayloadHandler.getData();
        }

        @Override
        public void terminate() {
        }

        @Override
        public boolean isTerminated() {
            return false;
        }
    }

    private static class RangeCheckerFactory {
        public static RangeChecker create(final OffsetRange range) {
            if (range == null) {
                return count -> true;
            }

            final long start = range.getOffset();
            final long end = range.getOffset() + range.getLength();
            return count -> count >= start && count < end;
        }
    }

    private static class OpenGroupsFactory {
        public static OpenGroups create(final String[] openGroups) {
            if (openGroups == null || openGroups.length == 0) {
                return group -> true;
            }

            final Set<String> set = new HashSet<>(Arrays.asList(openGroups));
            return key -> key != null && set.contains(key.toString());
        }
    }
}