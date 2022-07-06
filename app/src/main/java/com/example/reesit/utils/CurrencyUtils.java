package com.example.reesit.utils;

public class CurrencyUtils {

    public static class CurrencyUtilsException extends Exception{
        public CurrencyUtilsException(String message, Throwable e){super(message, e);}
    }
    /** Converts an integer value to currency in a String e.g 1023 becomes 10.23
     * @param input integer value to be converted
     * @return currency output in a string
     */
    public static String integerToCurrency(Integer input){
        String stringVal = input.toString();
        return stringVal.substring(0, stringVal.length()-2)+"."+stringVal.substring(stringVal.length()-2);
    }

    /** Converts a string currency value to an integer e.g 10.23 becomes 1023.
     * Input must have two decimal points or less
     * @param input string currency value to be converted
     * @return integer output
     */
    public static Integer stringToCurrency(String input) throws CurrencyUtilsException {
        // check for foreign characters
        Double doubleVal;
        String convertedString;
        try{
            doubleVal = Double.valueOf(input);
        } catch(NumberFormatException e){
            throw new CurrencyUtilsException("invalid input", null);
        }

        // check that a decimal point exists, add 00 if it doesnt
        if (input.contains(".")){
            String[] parts = input.split("\\.");
            String mantissa = parts[1];
            if (mantissa.length() == 0){
                convertedString = parts[0] + "00";
            } else if (mantissa.length() == 1){
                convertedString = parts[0]+mantissa+"0";
            } else if (mantissa.length() == 2){
                convertedString = input.replaceAll("\\.", "");
            } else {
                throw new CurrencyUtilsException("Currency value should not have more than 2 decimal places", null);
            }
        } else {
            convertedString = input+"00";
        }

        return Integer.parseInt(convertedString);


        // check number of
    }
}
