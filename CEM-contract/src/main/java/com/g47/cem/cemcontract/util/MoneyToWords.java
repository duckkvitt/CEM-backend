package com.g47.cem.cemcontract.util;

public class MoneyToWords {

    private static final String[] units = { "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
    private static final String[] teens = { "mười", "mười một", "mười hai", "mười ba", "mười bốn", "mười lăm", "mười sáu", "mười bảy", "mười tám", "mười chín" };
    private static final String[] tens = { "", "mười", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi" };
    private static final String[] thousands = { "", "nghìn", "triệu", "tỷ" };

    public static String convert(double number) {
        long longPart = (long) number;
        if (longPart == 0) {
            return "Không đồng";
        }
        String result = convert(longPart);
        // Capitalize the first letter and add currency unit
        return result.substring(0, 1).toUpperCase() + result.substring(1) + " đồng chẵn";
    }

    private static String convert(long number) {
        if (number == 0) {
            return "";
        }

        String result = "";
        int i = 0;
        while (number > 0) {
            long chunk = number % 1000;
            if (chunk > 0) {
                result = convertChunk((int) chunk) + " " + thousands[i] + " " + result;
            }
            number /= 1000;
            i++;
        }
        return result.trim().replaceAll("\\s+", " ");
    }

    private static String convertChunk(int number) {
        if (number == 0) {
            return "";
        }

        if (number < 10) {
            return units[number];
        }

        if (number < 20) {
            return teens[number - 10];
        }

        if (number < 100) {
            int unit = number % 10;
            int ten = number / 10;
            String unitStr;
            if (unit == 1) {
                unitStr = "mốt";
            } else if (unit == 5) {
                unitStr = "lăm";
            } else {
                unitStr = units[unit];
            }
            return tens[ten] + (unit > 0 ? " " + unitStr : "");
        }

        int hundred = number / 100;
        int remainder = number % 100;
        String remainderStr = "";
        if (remainder > 0) {
            if (remainder < 10) {
                remainderStr = " linh " + units[remainder];
            } else {
                remainderStr = " " + convertChunk(remainder);
            }
        }
        return units[hundred] + " trăm" + remainderStr;
    }
} 