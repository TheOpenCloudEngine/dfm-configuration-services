package org.opencloudengine.dfm.services.configuration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Input Output Utility.
 */
public class IOUtils {

    public static String readAsString(InputStream is, boolean ignoreOnError) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.commons.io.IOUtils.copy(is, baos);
            return new String(baos.toByteArray(), "UTF-8");
        } catch (Exception e) {
            if (ignoreOnError) {
                return "";
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}