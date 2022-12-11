package com.kasperovich.javafxapp.repository.organization;

import com.kasperovich.javafxapp.domain.Organization;
import com.kasperovich.javafxapp.domain.User;
import com.kasperovich.javafxapp.domain.enums.OrgType;
import com.kasperovich.javafxapp.exception.NoSuchEntityException;
import com.kasperovich.javafxapp.exception.RecurringEmailException;
import com.kasperovich.javafxapp.repository.user.UserTableColumns;
import com.kasperovich.javafxapp.util.DBPropertiesReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                .build();
    }



    @Override
    public Organization findById(Long id) {
        return null;
    }

    @Override
    public Optional<Organization> findOne(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Organization> findAll() {
        return null;
    }

    @Override
    public List<Organization> findAll(Optional<Integer> limit, int offset) {
        final String findAllQuery = "select * from testjfx.organizations "
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

    @Override
    public Organization create(Organization object) {
        final String insertQuery =
                "insert into testjfx.organizations (type, name, employees_amount, creation_date, modification_date, user_id, is_deleted) " +
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
            ResultSet resultSet = connection.prepareStatement("SELECT currval('testjfx.organizations_id_seq') as last_id").executeQuery();
            resultSet.next();
            long userLastInsertId = resultSet.getLong("last_id");

            return findById(userLastInsertId);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
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
        final String findByIdQuery = "select * from testjfx.organizations where user_id = " + userId +
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
                "from testjfx.organizations" +
                " where organizations.user_id="+userId;
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
}
