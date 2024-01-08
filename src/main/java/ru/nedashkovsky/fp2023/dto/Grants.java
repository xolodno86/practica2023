package ru.nedashkovsky.fp2023.dto;

import lombok.Data;

@Data
public class Grants {
    private String company_name;
    private String street_name;
    private Integer grant_size;
    private Integer fiscal_year;
    private String business_type;
    private Integer number_of_workplaces;

    public Grants(String[] csvLine) {
        this.company_name = csvLine[0];
        this.street_name = csvLine[1];
        this.grant_size = parseInteger(csvLine[2]);
        this.fiscal_year = parseInteger(csvLine[3]);
        this.business_type = csvLine[4];
        this.number_of_workplaces = parseInteger(csvLine[5]);
    }

    private Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            s = s.replace("$", "").replace(",", "");
            String[] parts = s.split("\\.");
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}