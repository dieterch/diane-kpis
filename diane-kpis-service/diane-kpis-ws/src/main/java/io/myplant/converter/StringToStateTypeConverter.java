package io.myplant.converter;

import io.myplant.model.StateType;
import org.springframework.core.convert.converter.Converter;

public class StringToStateTypeConverter implements Converter<String, StateType> {
    @Override
    public StateType convert(String source) {
        return StateType.getNameByValue(Integer.parseInt(source));
    }
}
