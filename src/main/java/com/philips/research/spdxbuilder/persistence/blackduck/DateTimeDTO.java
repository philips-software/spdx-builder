package com.philips.research.spdxbuilder.persistence.blackduck;

import java.time.LocalDateTime;
import java.time.chrono.Chronology;

public class DateTimeDTO {

    private int dayOfYear;
    private String dayOfWeek;
    private String month;
    private int dayOfMonth;
    private int year;
    private int monthValue;
    private int hour;
    private int minute;
    private int second;
    private int nano;
    private Chronology Chronology;

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonthValue() {
        return monthValue;
    }

    public void setMonthValue(int monthValue) {
        this.monthValue = monthValue;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getNano() {
        return nano;
    }

    public void setNano(int nano) {
        this.nano = nano;
    }

    public java.time.chrono.Chronology getChronology() {
        return Chronology;
    }

    public void setChronology(java.time.chrono.Chronology chronology) {
        Chronology = chronology;
    }

    public LocalDateTime toDateTime (){
        return LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute);
    }
}
