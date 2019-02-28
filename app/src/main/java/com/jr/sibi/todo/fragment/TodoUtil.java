package com.jr.sibi.todo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sibi-4939 on 01/05/18.
 */

public class TodoUtil {

    public static String getFormatedDateFromMilliSec(Long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return dateformat.format(date);
    }
}
