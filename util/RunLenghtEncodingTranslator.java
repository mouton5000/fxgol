package util;

import java.util.regex.Matcher;

public class RunLenghtEncodingTranslator {

    public static boolean[][] fromRLE(String rleDescription){
        String[] lines = rleDescription.split("\\n");

        boolean[][] cells = null;

        int index;
        for(index = 0; index < lines.length; index++){
            String line = lines[index].trim();
            if(line.startsWith("#"))
                continue;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("x = (\\d+), y = (\\d+).*");
            Matcher m = p.matcher(line);
            if(m.matches()) {
                cells = new boolean[Integer.valueOf(m.group(2))][Integer.valueOf(m.group(1))];
                break;
            }

        }

        if(cells == null)
            return cells;

        int value = 0;
        int l = 0;
        int c = 0;
        for(int i = index + 1; i < lines.length; i++){
            String line = lines[i].trim();
            for(int j = 0; j < line.length(); j++){
                char ch = line.charAt(j);
                if(ch == 'b' || ch == 'o'){
                    if(value == 0)
                        value = 1;
                    if(ch == 'o') {
                        for (int k = c; k < c + value; k++)
                            cells[l][k] = true;
                    }
                    c += value;
                    value = 0;
                }
                else if(ch == '$'){
                    if(value == 0)
                        value = 1;
                    l += value;
                    c = 0;
                    value = 0;
                }
                else
                    value = 10 * value + Character.getNumericValue(ch);
            }
        }
        return cells;
    }

    public static String toRLE(boolean[][] cells){
        StringBuilder sb = new StringBuilder();
        sb.append("x = ");
        sb.append(cells[0].length);
        sb.append(", y = ");
        sb.append(cells.length);
        sb.append("\n");

        int length = 0;
        int value = 0;
        Boolean current = null;
        for(int i = 0; i < cells.length; i++){
            for(int j = 0; j < cells[0].length; j++){
                Boolean alive = cells[i][j];
                if(alive == current)
                    value += 1;
                else{
                    if(current != null){
                        if(length + Math.log10(value) + 1 > 70) {
                            sb.append('\n');
                            length = 0;
                        }
                        length += (int)Math.log10(value) + 1;
                        sb.append(value);
                        sb.append(current?'o':'b');
                    }
                    current = alive;
                    value = 1;
                }
            }
            if(current != null && current) {
                if (length + Math.log10(value) + 1 > 70){
                    sb.append('\n');
                    length = 0;
                }
                length += (int)Math.log10(value) + 1;
                sb.append(value);
                sb.append('o');
            }
            if(length + 1 > 70) {
                sb.append('\n');
                length = 0;
            }
            length += 1;
            sb.append('$');
            current = null;
            value = 0;
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append('!');

        return sb.toString();
    }
}
