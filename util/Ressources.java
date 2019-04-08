/*
 * Copyright (c) 2018 Dimitri Watel
 */

package util;

/**
 * Helper in order to simply access to the resource folder.
 */
public class Ressources {
    public static String getRessource(String filename){
        String res = Ressources.class.getClassLoader().getResource(filename).toExternalForm();
        if(res.startsWith("file:"))
            return res.substring(5);
        else
            return res;
    }
}
