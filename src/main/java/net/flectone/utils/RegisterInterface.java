package net.flectone.utils;

import java.lang.reflect.InvocationTargetException;

public interface RegisterInterface {

    void register(Class<?> fClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

}
