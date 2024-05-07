package com.lukemango.plotmines.util;

public class StringUtil {

    /**
     * Replaces underscores with spaces and capitalises the first letter of each word
     *
     * @param input The input string
     * @return The formatted string
     */
    public static String formatString(String input) {
        // Check if the input is null or empty
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Convert the input to lowercase
        String newString = input.toLowerCase();

        // Split the input into words using underscores as separators
        String[] words = newString.split("_");

        // Create a StringBuilder to build the result
        StringBuilder result = new StringBuilder();

        // Iterate through each word, capitalise the first letter, and append to the result
        for (String word : words) {
            if (!word.isEmpty()) {
                char firstLetter = Character.toUpperCase(word.charAt(0));
                result.append(firstLetter).append(word.substring(1)).append(" ");
            }
        }

        // Remove the trailing space and return the result
        return result.toString().trim();
    }

}
