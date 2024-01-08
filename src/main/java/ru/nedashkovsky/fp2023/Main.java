package ru.nedashkovsky.fp2023;

import com.opencsv.exceptions.CsvException;
import ru.nedashkovsky.fp2023.dto.Grants;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String filename = "Grants.csv";
        if (args.length > 0) {
            filename = args[0];
        }
        try {
            List<Grants> grants = new Parse().parseCsv(filename);
            processGrants(grants);
        } catch (IOException | CsvException | SQLException e) {
            e.printStackTrace();
        }
    }
    private static void processGrants(List<Grants> grants) throws SQLException {
        Database db = new Database();
        try {
            db.openConnection("is");
            fillDatabase(db, grants);
            generateAverageNumberOfJobs(db);
            printAverageGrantSizeForSalonBarbershop(db, "Salon/Barbershop");
            printBusinessTypeWithMostJobsUnderGrantSize(db, 55000);
        } finally {
            db.close();
        }
    }
    private static void fillDatabase(Database db, List<Grants> grants) throws SQLException {
        db.saveGrants(grants);
    }
    private static void generateAverageNumberOfJobs(Database db) throws SQLException {
        Map<Integer, Double> averageJobsData = db.getAverageNumberOfJobs();
        GrantsChart chart = new GrantsChart(averageJobsData);
        chart.setVisible(true);
    }
    private static void printAverageGrantSizeForSalonBarbershop(Database db, String arg) throws SQLException {
        double avgGrantSize = db.getAverageGrantSizeForBusinessType(arg);
        System.out.println("Средний размер гранта для " + arg + " : " + avgGrantSize);
    }
    private static void printBusinessTypeWithMostJobsUnderGrantSize(Database db, int arg) throws SQLException {
        String earthquakeState = db.getBusinessTypeWithMostJobsUnderGrantSize(arg);
        System.out.println(arg + " -> " + earthquakeState);
    }
}