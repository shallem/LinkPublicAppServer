package org.helix.mobile.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author frederic
 */
public interface JSONSerializable {
    public void toJSON(JSONGenerator jg) throws IOException,
                IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException,
                NoSuchMethodException;
}
