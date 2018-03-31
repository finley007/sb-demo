package com.example.demo.field;

import com.example.demo.field.in.DtInConverter;
import com.example.demo.field.out.DtOutConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongb on 2018/3/31.
 */
@Component
public class ConverterManager {

    public static final String FIELD_TYPE_DATETIME = "datetime";

    public ConverterManager() {
        in = new HashMap<String, IConverter>();
        in.put(FIELD_TYPE_DATETIME, new DtInConverter());
        out = new HashMap<String, IConverter>();
        out.put(FIELD_TYPE_DATETIME, new DtOutConverter());
    }

    private Map<String, IConverter> in;

    private Map<String, IConverter> out;

    public Map<String, IConverter> getIn() {
        return in;
    }

    public void setIn(Map in) {
        this.in = in;
    }

    public Map<String, IConverter> getOut() {
        return out;
    }

    public void setOut(Map out) {
        this.out = out;
    }
}
