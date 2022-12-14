package com.chumachenko.orgsinfo.repository.organization;


import com.chumachenko.orgsinfo.repository.util.DBPropertiesReader;
import entities.Organization;
import entities.enums.OrgType;
import exception.NoSuchEntityException;
import exception.RecurringOrgNameException;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class OrgRepoImpl implements OrgRepository{

    private Organization orgRowMapping(ResultSet rs) throws SQLException {

        return Organization.builder()
                .id(rs.getLong(OrgTableColumns.ID))
                .type(OrgType.valueOf(rs.getString(OrgTableColumns.TYPE)))
                .name(rs.getString(OrgTableColumns.NAME))
                .numberOfEmployees(rs.getInt(OrgTableColumns.EMPLOYEES_AMOUNT))
                .creationDate(rs.getTimestamp(OrgTableColumns.CREATED))
                .modificationDate(rs.getTimestamp(OrgTableColumns.CHANGED))
                .userId(rs.getLong(OrgTableColumns.USER_ID))
                .isDeleted(rs.getBoolean(OrgTableColumns.IS_DELETED))
                .solvency(rs.getDouble(OrgTableColumns.SOLVENCY))
                .liquidity(rs.getDouble(OrgTableColumns.LIQUIDITY))
                .build();
    }



    @Override
    public Organization findById(Long id) {
        return  null;
    }

    @Override
    public Optional<Organization> findOne(Long id) {

        final String findByEmailQuery = "select * from orgsinfo.organizations where id = "+id+" and is_deleted = false";

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findByEmailQuery);
            boolean hasRow = rs.next();
            if (hasRow) {
                return Optional.of(orgRowMapping(rs));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public List<Organization> findAll() {
        return null;
    }

    @Override
    public List<Organization> findAll(Optional<Integer> limit, int offset) {
        final String findAllQuery = "select * from orgsinfo.organizations "
                +
                "where is_deleted=false order by id limit " + limit + " offset " + offset;

        List<Organization> result = new ArrayList<>();

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findAllQuery);

            while (rs.next()) {
                result.add(orgRowMapping(rs));
            }

            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @SneakyThrows
    @Override
    public Organization create(Organization object){
        final String insertQuery =
                "insert into orgsinfo.organizations" +
                        "(type, name, employees_amount, creation_date, modification_date, user_id, is_deleted) " +
                        " values (?, ?, ?, ?, ?, ?, ?);";

        Connection connection;
        PreparedStatement statement;

        try {

            connection = DBPropertiesReader.getConnection();
            statement = connection.prepareStatement(insertQuery);

            statement.setString(1, object.getType().toString());
            statement.setString(2, object.getName());
            statement.setInt(3, Optional.ofNullable(object.getNumberOfEmployees()).orElse(0));
            statement.setTimestamp(4, new Timestamp(new Date().getTime()));
            statement.setTimestamp(5, null);
            statement.setLong(6, object.getUserId());
            statement.setBoolean(7,false);

            //executeUpdate - for INSERT, UPDATE, DELETE
            statement.executeUpdate();
            //For select
            //statement.executeQuery();

            /*Get users last insert id for finding new object in DB*/
            ResultSet resultSet = connection.prepareStatement("SELECT currval('orgsinfo.organizations_id_seq') as last_id").executeQuery();
            resultSet.next();
            long userLastInsertId = resultSet.getLong("last_id");

            return findById(userLastInsertId);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RecurringOrgNameException("This user already created organization with this name", 409);
        }
    }

    @Override
    public void deleteByUserIdAndName(Long userId, String name) {
        final String softDeleteQuery =
                "update orgsinfo.organizations " +
                        "set " +
                        "is_deleted=true" +
                        " where user_id="+userId +
                        " and organizations.name="+"'"+name+"'";

        Connection connection;
        Statement statement;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(softDeleteQuery);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Double updateLiquidity(Double liquidity, Long id) {
        final String updateQuery =
                "update orgsinfo.organizations" +
                        " set liquidity= "+liquidity+
                        " where id="+id;

        Connection connection;
        Statement statement;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(updateQuery);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
        return liquidity;
    }

    @Override
    public Double updateSolvency(Double solvency, Long id) {
        final String updateQuery =
                "update orgsinfo.organizations" +
                        " set solvency= "+solvency+
                        " where id="+id;

        Connection connection;
        Statement statement;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(updateQuery);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
        return solvency;
    }

    @Override
    public String changeOrgName(String name, Long id){
        final String updateQuery="update orgsinfo.organizations " +
             "set name = ? , modification_date= ? " +
                "where id= ? ";


        Connection connection;
        PreparedStatement statement;
        try{
            connection=DBPropertiesReader.getConnection();
            statement= connection.prepareStatement(updateQuery);
            statement.setString(1, name);
            statement.setTimestamp(2, new Timestamp(new Date().getTime()));
            statement.setLong(3, id);
            statement.executeUpdate();

        }
        catch (SQLException ex){
            System.err.println(ex.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
        return name;
    }

    @Override
    public Organization update(Organization object) {
        return null;
    }

    @Override
    public Long delete(Long id) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public List<Organization> findAllByUserId(Long userId) {
        final String findByIdQuery = "select * from orgsinfo.organizations where user_id = " + userId +
                " and is_deleted = false";

        List<Organization>result=new ArrayList<>();

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findByIdQuery);
            while (rs.next()) {
                result.add(orgRowMapping(rs));
            }
            if(result.isEmpty())throw new NoSuchEntityException("No organizations of this user", 404);
            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Long findNumberOfOrgsOfUser(Long userId) {
        final String findNumberQuery="select count(organizations)" +
                "from orgsinfo.organizations" +
                " where organizations.user_id="+userId
                +" and is_deleted=false";
        Long result;
        Connection connection;
        Statement statement;
        ResultSet rs;

        try{
            connection=DBPropertiesReader.getConnection();
            statement= connection.createStatement();
            rs=statement.executeQuery(findNumberQuery);
            rs.next();
            return rs.getLong("count");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Optional<Organization> findByUserIdAndName(Long userId, String name) {
        final String query =
                "select * from orgsinfo.organizations "+
                        " where user_id="+userId +
                        " and organizations.name="+"'"+name+"'and is_deleted=false";

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs=statement.executeQuery(query);
            if(rs.next())return Optional.of(orgRowMapping(rs));
            else return Optional.empty();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public List<Organization> findTopSortedByLiquidity() {
        final String findQuery =
                "select * from" +
                        " orgsinfo.organizations "+
                        "where is_deleted=false and liquidity IS NOT NULL " +
                        "order by liquidity desc limit 10";

        List<Organization> result = new ArrayList<>();

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findQuery);

            while (rs.next()) {
                result.add(orgRowMapping(rs));
            }

            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public List<Organization> findTopSortedBySolvency() {
        final String findQuery =
                "select * from" +
                " orgsinfo.organizations "+
                "where is_deleted=false and solvency IS NOT NULL " +
                "order by solvency desc limit 10";

        List<Organization> result = new ArrayList<>();

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findQuery);

            while (rs.next()) {
                result.add(orgRowMapping(rs));
            }

            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Double calculateAverageLiquidity() {
        final String findNumberQuery="select avg(orgsinfo.organizations.liquidity)" +
                "from orgsinfo.organizations" +
                " where liquidity IS NOT NULL and is_deleted=false";
        Double result;
        Connection connection;
        Statement statement;
        ResultSet rs;

        try{
            connection=DBPropertiesReader.getConnection();
            statement= connection.createStatement();
            rs=statement.executeQuery(findNumberQuery);
            rs.next();
            return rs.getDouble("avg");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Double calculateAverageSolvency() {
        final String findNumberQuery="select avg(orgsinfo.organizations.solvency)" +
                "from orgsinfo.organizations" +
                " where organizations.solvency IS NOT NULL and is_deleted=false";
        Double result;
        Connection connection;
        Statement statement;
        ResultSet rs;

        try{
            connection=DBPropertiesReader.getConnection();
            statement= connection.createStatement();
            rs=statement.executeQuery(findNumberQuery);
            rs.next();
            return rs.getDouble("avg");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }
}
