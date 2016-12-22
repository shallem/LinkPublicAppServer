package org.helix.mobile.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author frederic
 */
public interface JSONSerializable {
    public void toJSON(JsonGenerator jg) throws IOException,
                IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException,
                NoSuchMethodException;
}
