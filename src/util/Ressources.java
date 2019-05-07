/*
 * Copyright (c) 2018 Dimitri Watel
 */

package util;

import java.io.*;
import java.util.*;

/**
 * Helper in order to simply access to the resource folder.
 */
public class Ressources {

    public static List<String> getPatternsDirectories(){
        InputStream patternsFileStream = Ressources.class.getResourceAsStream("/patterns/listOfPatterns");
        Set<String> directories = new HashSet<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(patternsFileStream));
            String line;
            while((line = br.readLine()) != null)
                directories.add(new File(line).getParent());
            br.close();
            patternsFileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> list = new ArrayList<>(directories);
        Collections.sort(list);
        return list;
    }

    public static List<String> getPatternsOfDir(String dir) {
        InputStream patternsFileStream = Ressources.class.getResourceAsStream("/patterns/listOfPatterns");
        ArrayList<String> files = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(patternsFileStream));
            String line;
            while ((line = br.readLine()) != null)
                if (dir.equals(new File(line).getParent()))
                    files.add(line);
            br.close();
            patternsFileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(files);
        return files;
    }

    public static String getDescription(String patternFile){
        String description = null;
        BufferedReader br = null;
        InputStream patternStream = Ressources.class.getResourceAsStream(patternFile);
        try {
            br = new BufferedReader(new InputStreamReader(patternStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line + "\n");
            description = sb.toString();
            br.close();
            patternStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return description;
    }
}
