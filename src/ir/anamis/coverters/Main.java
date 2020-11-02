package ir.anamis.coverters;

import java.util.GregorianCalendar;

public class Main {

    public static void main(String[] args) {
        DateConverter c = new DateConverter();
        print(c.getShFullDate());
    }
    private static void print(Object...objects){
        for (Object s : objects)
            System.out.println(s);
    }
}
