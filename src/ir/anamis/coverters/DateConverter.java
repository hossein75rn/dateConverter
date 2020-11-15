package ir.anamis.coverters;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateConverter {
    public static final byte TYPE_SHAMSI = 0;
    public static final byte TYPE_GREGORIAN = 1;
    private final int[][] grMonthsDays = {{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365},
            {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366}};
    private final int[][] shMonthsDays = {{0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 365},
            {0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366}};
    private final String[] shMonthsNames = {"فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر",
            "آبان", "آذر", "دی", "بهمن", "اسفند"};
    private final String[] grMonthsNamesPer = {"ژانویه", "فوریه", "مارس", "آوریل", "می", "جون", "جولای",
            "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"};
    private final String[] shDaysNames = {"","دو شنبه","سه شنبه","چهار شنبه","پنج شنبه","جمعه","شنبه","یک شنبه"};
    private final String[] grMonthsNames = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};
    private final String[] grMonthsNamesAbbr = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
            "Sept", "Oct", "Nov", "Dec"};

    private int grYear, grMonth, grDay, shYear, shMonth, shDay;


    public DateConverter() {
        LocalDate localDate = LocalDate.now();
        this.grYear = localDate.getYear();
        this.grMonth = localDate.getMonthValue();
        this.grDay = localDate.getDayOfMonth();
        toPersian();
    }

    public DateConverter(int year, int month, int day, int dateType) {
        if (dateIsPersian(dateType)) {
            shYear = year;
            shMonth = month;
            shDay = day;
            toGregorian();
        } else if (dateIsGregorian(dateType)) {
            grYear = year;
            grMonth = month;
            grDay = day;
            toPersian();
        } else {
            throw new RuntimeException("شمسی برابر است با 0 و میلادی برابر است با 1 dateType ");
        }
    }

    public DateConverter(Date date) {
        grYear = date.getYear() + 1900;
        grMonth = date.getMonth() + 1;
        grDay = date.getDate();
        toPersian();
    }

    public DateConverter(LocalDate localDate) {
        grYear = localDate.getYear();
        grMonth = localDate.getMonthValue();
        grDay = localDate.getDayOfMonth();
        toPersian();
    }

    public DateConverter(Calendar calendar) {
        grYear = calendar.get(Calendar.YEAR);
        grMonth = calendar.get(Calendar.MONTH) + 1;
        grDay = calendar.get(Calendar.DAY_OF_MONTH);
        toPersian();
    }

    public static DateConverter parse(String date, int dateType) {
        return parse(date, "-", dateType);
    }

    public static DateConverter parse(String date, String separator, int dateType) {
        String[] dateStr = date.split(separator);
        return new DateConverter(strToInt(dateStr[0]), strToInt(dateStr[1]), strToInt(dateStr[2]), dateType);
    }

    private static int strToInt(String s) {
        return Integer.parseInt(s);
    }

    private void toGregorian() {
        grYearCalc();
        int grDays = yearsDays(grYear) + leapYearsDays(grYear);
        int leftDays = (allShDays() + getYearDiff()) - grDays;
        grMonthDayCalc(leftDays);
    }

    private void toPersian() {
        shYearCalc();
        int leftDays = allGrDays();
        int shDays = yearsDays(shYear) + leapYearsDays(shYear);
        leftDays -= shDays + getYearDiff();
        shMonthDayCalc(leftDays);
    }

    public String getShDate() {
        return this.shYear + "-" + this.shMonth + "-" + this.shDay;
    }

    public String getShDate(String separator) {
        return this.shYear + separator + this.shMonth + separator + this.shDay;
    }

    public String[] getShDateStringArray() {
        return new String[]{String.valueOf(shYear), String.valueOf(shMonth), String.valueOf(shDay)};
    }

    public int[] getShDateIntArray() {
        return new int[]{shYear, shMonth, shDay};
    }

    public String getGrDate() {
        return this.grYear + "-" + this.grMonth + "-" + this.grDay;
    }

    public String getGrDate(String separator) {
        return this.grYear + separator + this.grMonth + separator + this.grDay;
    }

    public String[] getGrDateStringArray() {
        return new String[]{String.valueOf(grYear), String.valueOf(grMonth), String.valueOf(grDay)};
    }

    public int[] getGrDateIntArray() {
        return new int[]{grYear, grMonth, grDay};
    }


    public Date getDate() {
        return new Date(grYear, grMonth, grDay);
    }

    public Calendar getCalendar() {
        return new Calendar.Builder().setDate(grYear, grMonth, grDay).build();
    }

    public GregorianCalendar getGrCalendar() {
        return new GregorianCalendar() {
            @Override
            public void setGregorianChange(Date date) {
                date = getDate();
                super.setGregorianChange(date);
            }
        };
    }

    public LocalDate getLocalDate() {
        return LocalDate.of(grYear, grMonth, grDay);
    }

    public String getShFullDate(){
        return getShDate()+" "+shDaysNames[getLocalDate().getDayOfWeek().getValue()];
    }

    /*
    اگر سال میلادی کبیسه باشد تا قبل از روز 19 مارس
    اختلاف سال ها 622 سال و از 20 مارس تا آخر سال 621 روز

    اگر سال میلادی کبیسه نباشد تا قبل از روز 20 مارس
    اختلاف سال ها 622 سال واز 21 مارس تا آخر سال 621 روز

     */
    private void shYearCalc() {
        if (gregorianDateIsLeapYea()) {
            if (monthIsBelowEqX(3) && dayIsBelowEqX(19)) {
                setShYearMinusDiff(622);
            } else {
                setShYearMinusDiff(621);
            }
        } else {
            if (monthIsBelowEqX(3) && dayIsBelowEqX(20)) {
                setShYearMinusDiff(622);
            } else {
                setShYearMinusDiff(621);
            }
        }
    }

    private int allGrDays() {
        int allDays = 0;
        allDays += yearsDays(grYear);
        allDays += leapYearsDays(grYear);
        if (gregorianDateIsLeapYea()) {
            allDays += grMonthsDays[1][grMonth - 1];
        } else {
            allDays += grMonthsDays[0][grMonth - 1];
        }
        allDays += grDay;
        return allDays;
    }

    private void shMonthDayCalc(int leftDays) {
        if (persianDateIsLeapYear()) {
            loopThroughShMonth(leftDays, 1);
        } else {
            loopThroughShMonth(leftDays, 0);
        }
    }


    private void grMonthDayCalc(int leftDays) {
        if (gregorianDateIsLeapYea()) {
            loopThroughGrMonth(leftDays, 1);
        } else {
            loopThroughGrMonth(leftDays, 0);
        }
    }


    private void loopThroughShMonth(int leftDays, int x) {
        for (int i = 1; i < shMonthsDays[x].length; i++) {
            if (leftDays < shMonthsDays[x][i] || i == 12) {
                shMonth = i;
                shDay = (leftDays - shMonthsDays[x][i - 1]);
                break;
            }
        }
    }

    private void loopThroughGrMonth(int leftDays, int x) {
        for (int i = 1; i < grMonthsDays[x].length; i++) {
            if (leftDays < grMonthsDays[x][i] || i == 12) {
                grMonth = i;
                grDay = (leftDays - grMonthsDays[x][i - 1]);
                break;
            }
        }
    }

    private int getYearDiff() {
        return gregorianDateIsLeapYea() ? 226900 : 226899;
    }


    /*

    اگر سال شمسی کبیسه باشد تا قبل از روز 11 دی
    اختلاف سال ها 621 سال و از 12 دی تا آخر سال 622 روز

    اگر سال شمسی کبیسه باشد تا قبل از روز 10 دی
    اختلاف سال ها 621 سال و از 11 دی تا آخر سال 622 روز
     */
    private void grYearCalc() {
        if (persianDateIsLeapYear()) {
            if (monthIsBelowEqX(10) && dayIsBelowEqX(10)) {
                setGrYearSumDiff(621);
            } else {
                setGrYearSumDiff(622);
            }
        } else {
            if (monthIsBelowEqX(10) && dayIsBelowEqX(11)) {
                setGrYearSumDiff(621);
            } else {
                setGrYearSumDiff(622);
            }
        }
    }


    private void setShYearMinusDiff(int diff) {
        this.shYear = (grYear - diff);
    }

    private void setGrYearSumDiff(int diff) {
        this.grYear = (shYear + diff);
    }

    private boolean monthIsBelowEqX(int x) {
        return grMonth <= x;
    }

    private boolean dayIsBelowEqX(int x) {
        return grDay <= x;
    }

    private int allShDays() {
        int allDays = 0;
        allDays += yearsDays(shYear);
        allDays += leapYearsDays(shYear);
        allDays += shMonthsDays[0][shMonth - 1];
        allDays += shDay;
        return allDays;
    }


    private int yearsDays(int year) {
        return (year - 1) * 365;
    }

    private boolean dateIsPersian(int dateType) {
        return dateType == TYPE_SHAMSI;
    }

    private boolean dateIsGregorian(int dateType) {
        return dateType == TYPE_GREGORIAN;
    }

    public boolean persianDateIsLeapYear() {
        int year = this.shYear + 1;
        return dateIsLeapYear(year);
    }

    public boolean gregorianDateIsLeapYea() {
        return dateIsLeapYear(grYear);
    }

    private int leapYearsDays(int year) {
        return year / 4;
    }

    private boolean dateIsLeapYear(int year) {
        return year % 400 == 0 || (year % 100) == 0 || year % 4 == 0;
    }

    public int getGrYear() {
        return grYear;
    }

    public void setGrYear(int grYear) {
        this.grYear = grYear;
        toPersian();
    }

    public int getGrMonth() {
        return grMonth;
    }

    public void setGrMonth(int grMonth) {
        this.grMonth = grMonth;
        toPersian();
    }

    public int getGrDay() {
        return grDay;
    }

    public void setGrDay(int grDay) {
        this.grDay = grDay;
        toPersian();
    }

    public int getShYear() {
        return shYear;
    }

    public void setShYear(int shYear) {
        this.shYear = shYear;
        toGregorian();
    }

    public int getShMonth() {
        return shMonth;
    }

    public void setShMonth(int shMonth) {
        this.shMonth = shMonth;
        toGregorian();
    }

    public int getShDay() {
        return shDay;
    }

    public void setShDay(int shDay) {
        this.shDay = shDay;
        toGregorian();
    }
}
