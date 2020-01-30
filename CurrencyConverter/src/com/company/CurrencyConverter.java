package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CurrencyConverter {
    public static String WELCOME = "Hello! Welcome to the Converter of Currencies! \n";
    public static String ORIG_CURR = "Which currency are you converting from?";
    public static String CONVERT_AMOUNT = "How much money do you want to convert?";
    public static String NEW_CURR = "Which currency would you like to convert it to?";
    public static String RESTART_EXIT = "\nInput Y if you want to convert another currency. Otherwise, input exit to close the program.";
    public static String STR_INP_ERR = "This is not a valid input! Try again.\n";
    public static String SAME_CURR_ERR = "Cannot convert to the same currency. Input another.";
    public static String ADD = "To add another currency to the list. Input add.\n";
    public static String EDIT = "To edit a currency on the list. Input edit.\n";
    public static String DELETE = "To delete a currency from the list. Input delete.\n";
    public static String START = "Or input convert to start the program.\n";
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        String fileName = "currencies.properties";
        Properties config = new Properties();

        try (InputStream stream = CurrencyConverter.class.getClassLoader().getResourceAsStream(fileName)) {
            config.load(CurrencyConverter.class.getClassLoader().getResourceAsStream(fileName));

            Properties exchange = new Properties();
            try (InputStream flow = CurrencyConverter.class.getClassLoader().getResourceAsStream("exchangeRate.properties")) {
                exchange.load(CurrencyConverter.class.getClassLoader().getResourceAsStream("exchangeRate.properties"));

                start(input, config, exchange);

                if(input.hasNext("Y")) {
                    start(input, config, exchange);
                }

                if(input.hasNext("exit")) {
                    System.out.println("***Terminated***");
                    System.exit(0);
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void start(Scanner input, Properties config, Properties exchange) {
        System.out.println(WELCOME);
        System.out.println(ADD);
        System.out.println(EDIT);
        System.out.println(DELETE);
        System.out.println(START);

        converter(input, config, exchange);

        System.out.println(RESTART_EXIT);
    }

    public static void converter(Scanner input, Properties config, Properties exchange) {
                Map<String, String> currencyMap = new HashMap<String, String>();

                refreshList(currencyMap, config);

                printCurrList(currencyMap);

                addEditRemoveCurrency(currencyMap, config, exchange);

                System.out.println("Would you like to add, edit or delete another currency?");

                while(input.hasNext("Y")) {
                    System.out.println(ADD);
                    System.out.println(EDIT);
                    System.out.println(DELETE);

                    String operation = input.nextLine();

                    while (!CurrencyBuilder.inOperationsEnum(operation)) {
                        System.out.println("Please input a proper operation.");
                        operation = input.nextLine();
                    }

                    addEditRemoveCurrency(currencyMap, config, exchange);
                    refreshList(currencyMap, config);

                    System.out.println("Would you like to add, edit or delete another currency?");
                }

                input.nextLine();

                System.out.println(ORIG_CURR);
                refreshList(currencyMap, config);
                printCurrList(currencyMap);
                String originalCurrency = input.next().toUpperCase();

                while (!CurrencyBuilder.inCurrencyMap(originalCurrency, currencyMap)) {
                    System.out.println("Please input an existing currency.");
                    originalCurrency = input.next().toUpperCase();
                }

                System.out.println(CONVERT_AMOUNT);

                while (!input.hasNextInt() && input.nextInt() > 0) {
                    System.out.println(STR_INP_ERR);
                    input.next();
                }

                double amount = input.nextInt();

                System.out.println(NEW_CURR);

                printCurrList(currencyMap);

                String newCurrency = input.next().toUpperCase();

                while (originalCurrency.equals(newCurrency) || !CurrencyBuilder.inCurrencyMap(newCurrency, currencyMap)) {
                    if (originalCurrency.equals(newCurrency)) {
                        System.out.println(SAME_CURR_ERR);
                        newCurrency = input.next().toUpperCase();
                    } else if (!CurrencyBuilder.inCurrencyMap(newCurrency, currencyMap)) {
                        System.out.println("Please input an existing currency.");
                        newCurrency = input.next().toUpperCase();
                    }
                }

                System.out.print(CurrencyBuilder.convert(amount, originalCurrency, newCurrency, exchange));
    }

    public static void printCurrList(Map<String, String> currencyMap) {
        for(Map.Entry mapElement : currencyMap.entrySet()) {
            String key = (String)mapElement.getKey();
            String value = (String)mapElement.getValue();
            System.out.println(key + ":" + " " + value);
        }
    }

    public static void refreshList(Map<String, String> currencyMap, Properties config) {
        Enumeration keys = config.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = config.getProperty(key);
            currencyMap.put(key, value);
        }
    }

    public static void addEditRemoveCurrency(Map<String, String> currencyMap, Properties config, Properties exchange) {
        String key;
        String value;
        String operation = input.nextLine();

        while (!CurrencyBuilder.inOperationsEnum(operation)) {
            System.out.println("Please input a proper operation.");
            operation = input.nextLine();
        }

        switch (operation) {
            case "add":
                System.out.println("Input the abbreviation for the currency you are adding.");
                key = input.nextLine().toUpperCase();
                System.out.println("Input the description of your new currency.");
                value = input.nextLine();
                CurrencyBuilder.addCurrency(key, value, currencyMap, config);
                CurrencyBuilder.addExchangeRate(exchange, config, key);
                break;
            case "edit":
                System.out.println("Which currency in the list would you like to edit?");
                key = input.nextLine();
                while (!CurrencyBuilder.inCurrencyMap(key, currencyMap)) {
                    System.out.println("Please input an existing currency.");
                    key = input.next().toUpperCase();
                }
                CurrencyBuilder.editCurrency(key, currencyMap, config, exchange);
                break;
            case "delete":
                System.out.println("Input the currency abbreviation you would like to delete.");
                printCurrList(currencyMap);
                String currency = input.nextLine();
                while (!CurrencyBuilder.inCurrencyMap(currency, currencyMap)) {
                    System.out.println("Please input an existing currency.");
                    currency = input.next().toUpperCase();
                }
                CurrencyBuilder.removeCurrency(currency, currencyMap, config, exchange);
                break;
            case "convert":
                break;
        }
    }

    public static void restart(String[] args) {
        main(args);
    }
}
