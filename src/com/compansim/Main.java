package com.compansim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {

    sealed interface SERole permits Employee {
        default String returnRoleName() {
            return "Software Engineer";
        }
    }

    non-sealed static class Employee implements SERole {
        int id;
        String name;
        double salary;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }
    }

    static String createCompanyTable =
            """
            CREATE TABLE EMPLOYERS
            (ID      INT      NOT NULL,
             NAME    TEXT     NOT NULL,
             AGE     INT      NOT NULL,
             ADDRESS CHAR(50),
             SALARY  REAL,
             PRIMARY KEY (ID))""";

    static String createDepartmentTable =
            """
            CREATE TABLE DEPARTMENT
            (ID     INT      NOT NULL,
             DEPT   CHAR(50) NOT NULL,
             EMP_ID INT      NOT NULL,
             PRIMARY KEY(ID))""";

    static String insertCompanies =
            """
            INSERT INTO EMPLOYERS VALUES (
            1, 'Allen', 25, 'Texas', 15000.00
            ),
            (
            2, 'Teddy', 23, 'Norway', 20000.00
            ),
            (
            3, 'Mark', 25, 'Rich-Mond ', 65000.00
            );
            """;

    static String dropTables =
            """
            drop table employers;
            drop table department;""";

    public static void main(String[] args) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb", "postgres", "postgres");

            c.setAutoCommit(false);
            stmt = c.createStatement();
            stmt.executeUpdate(dropTables);
            stmt.executeUpdate(createCompanyTable);
            stmt.executeUpdate(createDepartmentTable);
            stmt.executeUpdate(insertCompanies);
            c.commit();

            c.setAutoCommit(true);
            ResultSet rs = stmt.executeQuery("SELECT * FROM EMPLOYERS;");

            List<SERole> employers = new ArrayList<>();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("ID"));
                employee.setName(rs.getString("NAME"));
                employee.setSalary(rs.getDouble("SALARY"));
                employers.add(employee);
            }

            int randomEmp = (int) (Math.rint(Math.random()*2));
            if (employers.get(randomEmp) instanceof Employee employee) {
                var role = switch (employee.returnRoleName()) {
                    case "Software Engineer" -> "SE";
                    case "Business Analitic" -> "BA";
                    case "Tester" -> "QA";
                    default -> "Newcomer";
                };
                System.out.println(employee.getName() + " - " + role);
            }

            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
}
