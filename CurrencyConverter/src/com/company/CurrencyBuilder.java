package com.company;

import java.io.*;
import java.util.*;

public class CurrencyBuilder {
    private static final Scanner input = new Scanner(System.in);

    enum Operations {
        add("add"),
        edit("edit"),
        delete("delete"),
        convert("convert"),
        Y("Y");

        private static LinkedList<String> ops = new LinkedList<>();
        private String operation;

        Operations(String operation) {
            this.operation = operation;
        }

        static {
            for (Operations operationsEnum : Operations.values()) {
                ops.add(operationsEnum.operation);
            }
        }

        @Override public String toString() {
            return this.operation;
        }
    }

    public static boolean inOperationsEnum(String operation) {
        for(String ops: Operations.ops) {
            if(ops.contains(operation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean inCurrencyMap(String currency, Map<String, String> currencyMap) {
        for(Map.Entry mapElement : currencyMap.entrySet()) {
            if(mapElement.getKey().equals(currency)) {
                return true;
            }
        }
        return false;
    }

    public static void addCurrency(String key, String value, Map<String, String> currencyMap, Properties config) {
        currencyMap.put(key, value);
        config.setProperty(key, value);

        try {
            OutputStream os = new FileOutputStream("currencies.properties");
            config.store(os, null);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addExchangeRate(Properties exchange, Properties config, String key) {
        String exchangeRate;
        String exchangeKey;

        Set<String> prop = config.stringPropertyNames();
        for (String toCurrency : prop) {
            if (!toCurrency.equals(key)) {
                System.out.println("Input the exchange rate for " + key + " to " + toCurrency);
                exchangeRate = input.nextLine();
                exchangeKey = key + "." + toCurrency;
                exchange.setProperty(exchangeKey, exchangeRate);

                System.out.println("Input the exchange rate for " + toCurrency + " to " + key);
                exchangeRate = input.nextLine();
                exchangeKey = toCurrency + "." + key;
                exchange.setProperty(exchangeKey, exchangeRate);

                try {
                    OutputStream os = new FileOutputStream("exchangeRate.properties");
                    exchange.store(os, null);
                } catch (IOException ex) {
                    ex.printStackTrace();

                }
            }
        }
    }

    public static void editCurrency(String key, Map<String, String> currencyMap, Properties config, Properties exchange) {
        String newKey;
        String newValue;
        Set<String> prop = config.stringPropertyNames();
        String temp;

        for(String currency : prop) {
            if(currency.equals(key)) {
                System.out.println("Do you want to change the description? Input Y to change.");
                temp = input.nextLine();
                if(temp.equalsIgnoreCase("Y")) {
                    System.out.println("Input the new description.");
                    newValue = input.nextLine();
                }
                else {
                    newValue = config.getProperty(key);
                }

                config.setProperty(key, newValue);
                CurrencyConverter.refreshList(currencyMap, config);

                System.out.println("Do you want to change an exchange rate? Input Y to change.");
                while(input.hasNext("Y")) {
                    Enumeration keys = exchange.propertyNames();
                    while (keys.hasMoreElements()) {
                        String exRateKey = (String)keys.nextElement();
                        String value = exchange.getProperty(exRateKey);
                        System.out.println(exRateKey + ": " + value);
                    }

                    editExchangeRate(exchange);
                    System.out.println("Do you want to edit another exchange rate? Input Y or N.");
                }

                try {
                    OutputStream os = new FileOutputStream("currencies.properties");
                    config.store(os, null);
                } catch (IOException ex) {
                    ex.printStackTrace();

                }
            }
        }
    }

    public static void editExchangeRate(Properties exchange) {
        String firstCurr;
        String secondCurr;
        String exchangeKey;
        String exchangeRate;

        System.out.println("Input the first currency abbreviation.");
        input.next();
        firstCurr = input.next().toUpperCase();
        System.out.println("Input the second currency abbreviation.");
        secondCurr = input.next().toUpperCase();
        System.out.println("Input the new exchange rate.");
        exchangeRate = input.next();

        exchangeKey = firstCurr + "." + secondCurr;

        Set<String> exRate = exchange.stringPropertyNames();
        for(String rate : exRate) {
            if(rate.equals(exchangeKey)) {
                exchange.setProperty(exchangeKey, exchangeRate);
            }
        }

        try {
            OutputStream os = new FileOutputStream("exchangeRate.properties");
            exchange.store(os, null);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void removeCurrency(String currency, Map<String, String> currencyMap, Properties config, Properties exchange) {
        currencyMap.entrySet().removeIf(entry -> currency.equalsIgnoreCase(entry.getKey()));
        config.remove(currency);

        Set<String> exRate = exchange.stringPropertyNames();
        for(String rate : exRate) {
            if(rate.contains(currency)) {
                exchange.remove(rate);
            }
        }
    }

    public static double convert(double amount, String originalCurrency, String newCurrency, Properties exchange) {
        String exchangeRate = originalCurrency + "." + newCurrency;
        Enumeration keys = exchange.propertyNames();

        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.equals(exchangeRate)) {
                return amount * Double.parseDouble(exchange.getProperty(exchangeRate));
            }
        }

        return amount;
    }
}
