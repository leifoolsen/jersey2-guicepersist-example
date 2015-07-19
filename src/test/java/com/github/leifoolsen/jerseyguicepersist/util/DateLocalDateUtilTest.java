package com.github.leifoolsen.jerseyguicepersist.util;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DateLocalDateUtilTest {

    @Test
    public void testConversions() {
        Date d = new Date();

        assertThat(DateLocalDateUtil.toDate(d), equalTo(d));

        assertThat(DateLocalDateUtil.toDate(d.toInstant()), equalTo(d));

        Timestamp t = new Timestamp(d.getTime());
        assertThat(DateLocalDateUtil.toDate(t), equalTo(d));

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        LocalDate localDate = LocalDate.of(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));

        assertThat(localDate, equalTo(DateLocalDateUtil.dateToLocalDate(d)));

        Instant i = Instant.ofEpochMilli(d.getTime());
        assertThat(DateLocalDateUtil.toDate(i), equalTo(d));

        LocalDateTime localDateTime = DateLocalDateUtil.dateToLocalDateTime(d);
        assertThat(localDateTime.atZone(ZoneId.systemDefault()), equalTo(d.toInstant().atZone(ZoneId.systemDefault())));
    }
}
