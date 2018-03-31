package com.example.demo.field.out;

import com.example.demo.field.IConverter;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rongb on 2018/3/31.
 */
public class DtOutConverter implements IConverter {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public String convert(String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                return sdf.format(sdf.format(new Date(Long.valueOf(value))));
            } catch (Exception e) {
                return value;
            }
        } else {
            return value;
        }
    }

}
