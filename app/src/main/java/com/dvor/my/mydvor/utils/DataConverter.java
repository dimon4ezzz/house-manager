package com.dvor.my.mydvor.utils;
import android.icu.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DataConverter {
    public static String convert(String date) {
        String convertedDate = "";
        Date currentDate = new Date();
        Date postDate= new Date();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
        try {
            postDate = format.parse(date);
        }


        catch(Exception ignored) { }

        int minutes=postDate.getMinutes();
        String minutesStr;
        if (minutes<10)
            minutesStr="0"+ minutes;
        else
            minutesStr=Long.toString(minutes);


        //если сообщение отправлено сегодня. Возвращаем время
        if (currentDate.getYear()==postDate.getYear() && currentDate.getMonth()==postDate.getMonth()
        && currentDate.getDay()==postDate.getDay())

            return postDate.getHours() +":"+minutesStr;

        // вчера
        if (currentDate.getYear()==postDate.getYear() && currentDate.getMonth()==postDate.getMonth()
                && currentDate.getDay()==(postDate.getDay()+1))
        return  "Вчера " + postDate.getHours() +":"+minutesStr;

        // раньше
        Calendar cal = Calendar.getInstance();
        cal.setTime(postDate);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        return day +"."+ month +"."+ year;
    }
}
