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
 */

package stroom.pipeline.xsltfunctions;


import org.junit.jupiter.api.Test;
import stroom.meta.shared.Meta;
import stroom.pipeline.state.MetaHolder;
import stroom.util.date.DateUtil;
import stroom.test.common.util.test.StroomUnitTest;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestFormatDate extends StroomUnitTest {
    @Test
    void testDayOfWeekAndWeekAndWeakYear() {
        final FormatDate formatDate = createFormatDate("2010-03-01T12:45:22.643Z");
        assertThat(test(formatDate, "ccc/w/YYYY", "Mon/1/2018")).isEqualTo("2018-01-01T00:00:00.000Z");
        assertThat(test(formatDate, "E/w/YYYY", "Mon/1/2018")).isEqualTo("2018-01-01T00:00:00.000Z");
    }

    @Test
    void testDayOfWeekAndWeek() {
        final FormatDate formatDate = createFormatDate("2010-03-04T12:45:22.643Z");
        assertThat(test(formatDate, "ccc/w", "Fri/2")).isEqualTo("2010-01-08T00:00:00.000Z");
        assertThat(test(formatDate, "E/w", "Fri/2")).isEqualTo("2010-01-08T00:00:00.000Z");
        assertThat(test(formatDate, "ccc/w", "Fri/40")).isEqualTo("2009-10-02T00:00:00.000Z");
        assertThat(test(formatDate, "E/w", "Fri/40")).isEqualTo("2009-10-02T00:00:00.000Z");
    }

    @Test
    void testDayOfWeek() {
        final FormatDate formatDate = createFormatDate("2010-03-04T12:45:22.643Z");
        assertThat(test(formatDate, "ccc", "Mon")).isEqualTo("2010-03-01T00:00:00.000Z");
        assertThat(test(formatDate, "E", "Mon")).isEqualTo("2010-03-01T00:00:00.000Z");
        assertThat(test(formatDate, "ccc", "Fri")).isEqualTo("2010-02-26T00:00:00.000Z");
        assertThat(test(formatDate, "E", "Fri")).isEqualTo("2010-02-26T00:00:00.000Z");
    }

    @Test
    void testParseManualTimeZones() {
        long date;

        final FormatDate formatDate = new FormatDate(null);

        date = formatDate.parseDate(null, "2001/08/01", "yyyy/MM/dd", "-07:00");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-08-01T07:00:00.000Z");

        date = formatDate.parseDate(null, "2001/08/01 01:00:00", "yyyy/MM/dd HH:mm:ss", "-08:00");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-08-01T09:00:00.000Z");

        date = formatDate.parseDate(null, "2001/08/01 01:00:00", "yyyy/MM/dd HH:mm:ss", "+01:00");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-08-01T00:00:00.000Z");
    }

    @Test
    void testParse() {
        long date;

        final FormatDate formatDate = new FormatDate(null);

        date = formatDate.parseDate(null, "2001/01/01", "yyyy/MM/dd", null);
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-01-01T00:00:00.000Z");

        date = formatDate.parseDate(null, "2001/08/01", "yyyy/MM/dd", "GMT");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-08-01T00:00:00.000Z");

        date = formatDate.parseDate(null, "2001/08/01 00:00:00.000", "yyyy/MM/dd HH:mm:ss.SSS", "GMT");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-08-01T00:00:00.000Z");

        date = formatDate.parseDate(null, "2001/08/01 00:00:00", "yyyy/MM/dd HH:mm:ss", "Europe/London");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-07-31T23:00:00.000Z");

        date = formatDate.parseDate(null, "2001/01/01", "yyyy/MM/dd", "GMT");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2001-01-01T00:00:00.000Z");

        date = formatDate.parseDate(null, "2008/08/08:00:00:00", "yyyy/MM/dd:HH:mm:ss", "Europe/London");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2008-08-07T23:00:00.000Z");

        date = formatDate.parseDate(null, "2008/08/08", "yyyy/MM/dd", "Europe/London");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo("2008-08-07T23:00:00.000Z");
    }

    @Test
    void testParseGMTBSTGuess() {
        assertThatThrownBy(() -> {
            doGMTBSTGuessTest(null, "");
        }).isInstanceOf(RuntimeException.class);

        // Winter
        doGMTBSTGuessTest("2011-01-01T00:00:00.999Z", "2011/01/01 00:00:00.999");

        // MID Point Summer Time 1 Aug
        doGMTBSTGuessTest("2001-08-01T03:00:00.000Z", "2001/08/01 04:00:00.000");
        doGMTBSTGuessTest("2011-08-01T03:00:00.000Z", "2011/08/01 04:00:00.000");

        // Boundary WINTER TO SUMMER
        doGMTBSTGuessTest("2011-03-26T22:59:59.999Z", "2011/03/26 22:59:59.999");
        doGMTBSTGuessTest("2011-03-26T23:59:59.999Z", "2011/03/26 23:59:59.999");
        doGMTBSTGuessTest("2011-03-27T00:00:00.000Z", "2011/03/27 00:00:00.000");
        doGMTBSTGuessTest("2011-03-27T00:59:59.000Z", "2011/03/27 00:59:59.000");
        // Lost an hour!
        doGMTBSTGuessTest("2011-03-27T00:00:00.000Z", "2011/03/27 00:00:00.000");
        doGMTBSTGuessTest("2011-03-27T01:59:00.999Z", "2011/03/27 01:59:00.999");
        doGMTBSTGuessTest("2011-03-27T02:00:00.999Z", "2011/03/27 03:00:00.999");

        // Boundary SUMMER TO WINTER
        doGMTBSTGuessTest("2011-10-29T23:59:59.999Z", "2011/10/30 00:59:59.999");
    }

    private void doGMTBSTGuessTest(final String expected, final String value) {
        final FormatDate formatDate = new FormatDate(null);
        final long date = formatDate.parseDate(null, value, "yyyy/MM/dd HH:mm:ss.SSS", "GMT/BST");
        assertThat(DateUtil.createNormalDateTimeString(date)).isEqualTo(expected);
    }

    @Test
    void testDateWithNoYear() {
        final FormatDate formatDate = createFormatDate("2010-03-01T12:45:22.643Z");

        assertThat(test(formatDate, "dd/MM", "01/01")).isEqualTo("2010-01-01T00:00:00.000Z");
        assertThat(test(formatDate, "dd/MM", "01/04")).isEqualTo("2009-04-01T00:00:00.000Z");
        assertThat(test(formatDate, "MM", "01")).isEqualTo("2010-01-01T00:00:00.000Z");
        assertThat(test(formatDate, "MM", "04")).isEqualTo("2009-04-01T00:00:00.000Z");
        assertThat(test(formatDate, "dd", "01")).isEqualTo("2010-03-01T00:00:00.000Z");
        assertThat(test(formatDate, "dd", "04")).isEqualTo("2010-02-04T00:00:00.000Z");
        assertThat(test(formatDate, "HH", "12")).isEqualTo("2010-03-01T12:00:00.000Z");
        assertThat(test(formatDate, "HH:mm", "12:30")).isEqualTo("2010-03-01T12:30:00.000Z");
    }

    @Test
    void testCaseSensitivity_upperCaseMonth() {
        ZonedDateTime time = parseUtcDate("dd-MMM-yy", "18-APR-18");
        assertThat(time.getMonth()).isEqualTo(Month.APRIL);
    }

    @Test
    void testCaseSensitivity_sentenceCaseMonth() {
        ZonedDateTime time = parseUtcDate("dd-MMM-yy", "18-Apr-18");
        assertThat(time.getMonth()).isEqualTo(Month.APRIL);
    }

    @Test
    void testCaseSensitivity_lowerCaseMonth() {
        ZonedDateTime time = parseUtcDate("dd-MMM-yy", "18-apr-18");
        assertThat(time.getMonth()).isEqualTo(Month.APRIL);
    }

    @Test
    void testWithTimeZoneInStr1() {
        ZonedDateTime time = parseUtcDate("dd-MM-yy HH:mm:ss xxx", "18-04-18 01:01:01 +00:00");
    }

    @Test
    void testWithTimeZoneInStr2() {
        parseUtcDate("dd-MM-yy HH:mm:ss Z", "18-04-18 01:01:01 +0000");
    }

    @Test
    void testWithTimeZoneInStr3() {
        parseUtcDate("dd-MM-yy HH:mm:ss Z", "18-04-18 01:01:01 -0000");
    }

    @Test
    void testWithTimeZoneInStr4() {
        parseUtcDate("dd-MM-yy HH:mm:ss xxx", "18-04-18 01:01:01 -00:00");
    }

    @Test
    void testWithTimeZoneInStr5() {
        parseUtcDate("dd-MM-yy HH:mm:ss VV", "18-04-18 01:01:01 Europe/London");
    }

    private ZonedDateTime parseUtcDate(final String pattern, final String dateStr) {
        final FormatDate formatDate = createFormatDate("2010-03-01T12:45:22.643Z");

        long timeMs = formatDate.parseDate(null, dateStr, pattern, "UTC");
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeMs), ZoneOffset.UTC);
    }

    private String test(final FormatDate formatDate, final String pattern, final String date) {
        return DateUtil.createNormalDateTimeString(formatDate.parseDate(null, date, pattern, "UTC"));
    }

    private FormatDate createFormatDate(final String referenceDate) {
        final Meta meta = mock(Meta.class);
        when(meta.getCreateMs()).thenReturn(DateUtil.parseNormalDateTimeString(referenceDate));

        final MetaHolder metaHolder = new MetaHolder();
        metaHolder.setMeta(meta);

        return new FormatDate(metaHolder);
    }
}
