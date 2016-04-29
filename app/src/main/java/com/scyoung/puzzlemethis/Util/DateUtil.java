package com.scyoung.puzzlemethis.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by scyoung on 4/27/16.
 */
public class DateUtil {

    public static String getDateString() {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormatter.format(date);
    }
}
