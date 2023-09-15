package net.flectone.utils;

import net.flectone.managers.FileManager;
import net.flectone.misc.files.FYamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

This file is part of DonationExecutor
https://github.com/link1107/DonationExecutor/blob/master/src/main/java/igorlink/service/Utils.java

DonationExecutor
Copyright (C) 2022 link1107

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

public class BlackListUtil {

    private static final List<String> swears = new ArrayList<>();
    private static final HashMap<Character, List<Character>> synonymousChars = new HashMap<>();

    static {
        loadSwears();

        synonymousChars.put('h', (Arrays.asList('x', 'х', 'н', 'n')));
        synonymousChars.put('n', (Arrays.asList('н', 'й', 'и')));
        synonymousChars.put('н', (Arrays.asList('h', 'n', 'й', 'и')));
        synonymousChars.put('e', (Arrays.asList('е', '3', 'з')));
        synonymousChars.put('е', (Arrays.asList('e', '3', 'з')));
        synonymousChars.put('г', (Arrays.asList('r', 'я', 'g', '7', '6')));
        synonymousChars.put('r', (Arrays.asList('г', 'я', 'g', '7', '6')));
        synonymousChars.put('g', (Arrays.asList('г', 'r', '7', '6')));
        synonymousChars.put('p', (Arrays.asList('п', 'р', 'n', 'я', 'r')));
        synonymousChars.put('р', (Arrays.asList('p', 'r', 'я')));
        synonymousChars.put('п', (Arrays.asList('p', 'n', 'и', 'р')));
        synonymousChars.put('o', (Arrays.asList('о', '0')));
        synonymousChars.put('о', (Arrays.asList('o', '0')));
        synonymousChars.put('a', (List.of('а')));
        synonymousChars.put('а', (List.of('a')));
        synonymousChars.put('и', (Arrays.asList('i', 'n', 'e', 'е', '|', 'l', '!', '1', '3', 'й')));
        synonymousChars.put('i', (Arrays.asList('1', 'и', 'e', 'е', '|', 'l', '!', 'й')));
        synonymousChars.put('с', (Arrays.asList('c', 's', '$', '5')));
        synonymousChars.put('s', (Arrays.asList('c', 'с', '$', '5')));
        synonymousChars.put('c', (Arrays.asList('s', 'с', '$', '5')));
        synonymousChars.put('л', (Arrays.asList('l', '1', '|')));
        synonymousChars.put('l', (Arrays.asList('л', '1', '|', '!')));
        synonymousChars.put('1', (Arrays.asList('л', 'i', 'l', '|')));
        synonymousChars.put('d', (Arrays.asList('д', 'л')));
        synonymousChars.put('д', (Arrays.asList('d', 'л', '9')));
        synonymousChars.put('y', (Arrays.asList('у', 'u', 'ы')));
        synonymousChars.put('у', (Arrays.asList('y', 'u', 'ы')));
        synonymousChars.put('x', (Arrays.asList('х', 'h')));
        synonymousChars.put('х', (Arrays.asList('x', 'h')));
        synonymousChars.put('ы', (Arrays.asList('у', 'u', 'y')));
        synonymousChars.put('ч', (List.of('4')));
        synonymousChars.put('k', (List.of('к')));
        synonymousChars.put('к', (List.of('k')));
        synonymousChars.put('0', (Arrays.asList('о', 'o')));
        synonymousChars.put('3', (Arrays.asList('e', 'е','з')));
        synonymousChars.put('4', (List.of('ч')));
        synonymousChars.put('5', (Arrays.asList('с', 'c', 's')));
        synonymousChars.put('9', (Arrays.asList('r', 'я')));
    }

    public static void loadSwears() {
        FYamlConfiguration swearsYaml = FileManager.load("swears.yml");
        swears.clear();
        swears.addAll(swearsYaml.getStringList("list"));
    }

    public static Boolean contains(String text) {

        String validationText = text.toLowerCase();

        Pattern pattern = Pattern.compile("[l1i]*[\\-]*[l1i]*");
        Matcher matcher = pattern.matcher(validationText);
        if ( (matcher.find()) && (!matcher.group().isEmpty()) ) {
            validationText = validationText.replace(matcher.group(), "н");
        }

        validationText = validationText.replace("_", "");
        validationText = validationText.replace(" ", "");
        validationText = validationText.replace(",", "");
        validationText = validationText.replace(".", "");
        validationText = validationText.replace("-", "");
        validationText = validationText.replace("%", "");
        validationText = validationText.replace("*", "");
        validationText = validationText.replace("?", "");

        if (validationText.isEmpty()) {
            return false;
        }


        if (!(validationText.matches("[a-zа-я0-9$!ё]*"))) {
            return true;
        }

        for (String ss : swears) {
            for (int i = 0; i <= validationText.length() - ss.length(); i++) {
                int tempi = i;
                for (int j = 0; j <= ss.length(); j++) {

                    if (j == ss.length()) {
                        return true;
                    }

                    if (validationText.charAt(tempi + j) == ss.charAt(j)) {
                        continue;
                    } else if ((synonymousChars.containsKey(ss.charAt(j))) && (synonymousChars.get(ss.charAt(j)).contains(validationText.charAt(tempi + j)))) {
                        continue;
                    }

                    while (true) {
                        if (j==0) {
                            break;
                        }
                        if (validationText.charAt(tempi + j) != validationText.charAt(tempi + j - 1)) {
                            if (!(synonymousChars.containsKey(validationText.charAt(tempi + j)))) {
                                break;
                            } else if (!(synonymousChars.get(validationText.charAt(tempi + j)).contains(validationText.charAt(tempi + j - 1)))) {
                                break;
                            }
                        }
                        tempi++;
                        if ((validationText.length()-tempi-j) < (ss.length()-j)) {
                            break;
                        }
                    }

                    if ((validationText.length()-tempi-j) < (ss.length()-j)) {
                        break;
                    }

                    if (validationText.charAt(tempi + j) == ss.charAt(j)) {
                        continue;
                    } else if ((synonymousChars.containsKey(ss.charAt(j)))) {
                        if ((synonymousChars.get(ss.charAt(j)).contains(validationText.charAt(tempi + j)))) {
                            continue;
                        }
                    }

                    break;

                }
            }
        }

        return false;
    }
}
