package ru.nedashkovsky.fp2023;

import ru.nedashkovsky.fp2023.dto.Grants;

import java.sql.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private Connection conn;
    private static final String CREATE_COMPANIES_TABLE = "CREATE TABLE IF NOT EXISTS companies (" +
            "company_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "company_name TEXT NOT NULL, " +
            "business_type TEXT NOT NULL)";
    private static final String CREATE_ADDRESSES_TABLE = "CREATE TABLE IF NOT EXISTS addresses (" +
            "street_name TEXT PRIMARY KEY)";
    private static final String CREATE_GRANTS_TABLE = "CREATE TABLE IF NOT EXISTS grants (" +
            "grant_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "grant_size INTEGER NOT NULL, " +
            "fiscal_year INTEGER, " +
            "company_id INTEGER, " +
            "job_count INTEGER, " +
            "FOREIGN KEY(company_id) REFERENCES companies(company_id))";
    private static final String INSERT_COMPANY = "INSERT OR IGNORE INTO companies (company_name, business_type) VALUES (?, ?)";
    private static final String INSERT_ADDRESS = "INSERT OR IGNORE INTO addresses (street_name) VALUES (?)";
    private static final String INSERT_GRANT = "INSERT OR IGNORE INTO grants (grant_size, fiscal_year, company_id, job_count) VALUES (?, ?, ?, ?)";

    // Открытие соединения с базой данных
    public void openConnection(String dbFileName) throws SQLException {
        String url = "jdbc:sqlite:" + dbFileName + ".db";
        conn = DriverManager.getConnection(url);
    }
    // Закрытие соединения с базой данных
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    // Создание таблиц, если они еще не созданы
    public void createStatesTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_COMPANIES_TABLE);
            stmt.execute(CREATE_ADDRESSES_TABLE);
            stmt.execute(CREATE_GRANTS_TABLE);
        }
    }
    // Сохранение данных о грантах в базу данных
    public void saveGrants(List<Grants> grantsList) throws SQLException {
        conn.setAutoCommit(false);
        createStatesTable();
        for (Grants grant : grantsList) {
            // Вставляем данные о компании и получаем её ID
            int companyId = insertCompanyAndGetId(grant);
            // Вставляем адрес, если он новый
            try (PreparedStatement pstmtAddress = conn.prepareStatement(INSERT_ADDRESS)) {
                pstmtAddress.setString(1, grant.getStreet_name());
                pstmtAddress.execute();
            }
            // Вставляем данные о гранте
            try (PreparedStatement pstmtGrant = conn.prepareStatement(INSERT_GRANT)) {
                Integer grantSize = grant.getGrant_size();
                if (grantSize != null) {
                    pstmtGrant.setInt(1, grantSize);
                } else {
                    pstmtGrant.setNull(1, Types.INTEGER);
                }
                Integer fiscalYear = grant.getFiscal_year();
                if (fiscalYear != null) {
                    pstmtGrant.setInt(2, fiscalYear);
                } else {
                    pstmtGrant.setNull(2, Types.INTEGER);
                }
                Integer jobСount = grant.getNumber_of_workplaces();
                if (jobСount != null) {
                    pstmtGrant.setInt(3, jobСount);
                } else {
                    pstmtGrant.setNull(3, Types.INTEGER);
                }
                pstmtGrant.setInt(4, companyId);
                pstmtGrant.execute();
            }
        }

        conn.commit();
        conn.setAutoCommit(true);
    }
    // Вставка компании и получение её ID
    private int insertCompanyAndGetId(Grants grant) throws SQLException {
        try (PreparedStatement pstmtCompany = conn.prepareStatement(INSERT_COMPANY)) {
            pstmtCompany.setString(1, grant.getCompany_name());
            pstmtCompany.setString(2, grant.getBusiness_type());
            pstmtCompany.execute();
        }

        // Получение последнего добавленного ID компании
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1); // Возврат последнего ID компании
            } else {
                throw new SQLException("Не удалось получить company_id.");
            }
        }
    }
    public Map<Integer, Double> getAverageNumberOfJobs() throws SQLException {
        Map<Integer, Double> averageJobsByYear = new HashMap<>();
        String query = "SELECT fiscal_year, AVG(job_count) as avg_jobs FROM grants WHERE fiscal_year != 0 GROUP BY fiscal_year";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int year = rs.getInt("fiscal_year");
                double avgJobs = rs.getDouble("avg_jobs");
                averageJobsByYear.put(year, avgJobs);
            }
        }
        return averageJobsByYear;
    }
    public double getAverageGrantSizeForBusinessType(String businessType) throws SQLException {
        String query = "SELECT AVG(CAST(grant_size AS REAL)) as avg_grant_size FROM grants INNER JOIN companies ON grants.company_id = companies.company_id WHERE business_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, businessType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_grant_size");
                }
            }
        }
        return 0;
    }
    public String getBusinessTypeWithMostJobsUnderGrantSize(int maxGrantSize) throws SQLException {
        String query = "SELECT companies.business_type, SUM(grants.job_count) as total_jobs " +
                "FROM grants " +
                "INNER JOIN companies ON grants.company_id = companies.company_id " +
                "WHERE grants.grant_size <= ? " +
                "GROUP BY companies.business_type " +
                "ORDER BY total_jobs DESC " +
                "LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maxGrantSize);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("business_type");
                }
            }
        }
        return null;
    }
}