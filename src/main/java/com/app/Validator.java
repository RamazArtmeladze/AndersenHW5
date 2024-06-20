package com.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Validator {

    private static int totalTickets = 0;
    private static int validTickets = 0;
    private static Map<String, Integer> violations = new HashMap<>();
    private static final List<String> VALID_TICKET_TYPES = Arrays.asList("DAY", "WEEK", "MONTH", "YEAR");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("Enter the file path or type 'END' to input tickets manually:");

        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("END")) {
            processManualInput(scanner, objectMapper);
        } else {
            try {
                processFileInput(input, objectMapper);
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + input);
            }
        }

        printStatistics();
        scanner.close();
    }

    private static void processManualInput(Scanner scanner, ObjectMapper objectMapper) {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.trim().equalsIgnoreCase("END")) {
                break;
            }
            if (input.trim().isEmpty()) {
                continue;
            }
            processTicket(input, objectMapper);
        }
    }

    private static void processFileInput(String filePath, ObjectMapper objectMapper) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filePath));
        while (fileScanner.hasNextLine()) {
            String input = fileScanner.nextLine();
            if (input.trim().isEmpty()) {
                continue;
            }
            processTicket(input, objectMapper);
        }
        fileScanner.close();
    }

    private static void processTicket(String input, ObjectMapper objectMapper) {
        try {
            BusTicket ticket = objectMapper.readValue(input, BusTicket.class);

            boolean isValid = validateTicket(ticket);

            if (isValid) {
                validTickets++;
            }

            System.out.println(ticket);
            totalTickets++;
        } catch (JsonProcessingException e) {
            System.err.println("Invalid input format: " + input);
        }
    }

    private static boolean validateTicket(BusTicket ticket) {
        boolean isValid = true;


        if (ticket.getStartDate() != null && !ticket.getStartDate().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = sdf.parse(ticket.getStartDate());
                if (startDate.after(new Date())) {
                    isValid = false;
                    recordViolation("start date");
                }
            } catch (ParseException e) {
                isValid = false;
                recordViolation("start date");
            }
        }

        if (!VALID_TICKET_TYPES.contains(ticket.getTicketType())) {
            isValid = false;
            recordViolation("ticket type");
        }

        try {
            int price = Integer.parseInt(ticket.getPrice());
            if (price % 2 != 0) {
                isValid = false;
                recordViolation("price");
            }
        } catch (NumberFormatException e) {
            isValid = false;
            recordViolation("price");
        }

        if (("DAY".equals(ticket.getTicketType()) || "WEEK".equals(ticket.getTicketType()) || "YEAR".equals(ticket.getTicketType()))
                && (ticket.getStartDate() == null || ticket.getStartDate().isEmpty())) {
            isValid = false;
            recordViolation("start date");
        }

        if (ticket.getPrice() == null || ticket.getPrice().isEmpty() || Integer.parseInt(ticket.getPrice()) == 0) {
            isValid = false;
            recordViolation("price");
        }

        if (ticket.getTicketType() == null || ticket.getTicketType().isEmpty()) {
            isValid = false;
            recordViolation("ticket type");
        }

        return isValid;
    }

    private static void recordViolation(String violationType) {
        violations.put(violationType, violations.getOrDefault(violationType, 0) + 1);
    }

    private static void printStatistics() {
        System.out.println("Total = " + totalTickets);
        System.out.println("Valid = " + validTickets);

        String mostPopularViolation = violations.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        System.out.println("Most popular violation = " + mostPopularViolation);
    }
}