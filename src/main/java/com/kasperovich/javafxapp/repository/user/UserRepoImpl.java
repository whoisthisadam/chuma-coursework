package com.kasperovich.javafxapp.repository.user;

import com.kasperovich.javafxapp.domain.User;
import com.kasperovich.javafxapp.exception.NoSuchEntityException;
import com.kasperovich.javafxapp.exception.RecurringEmailException;
import com.kasperovich.javafxapp.util.DBPropertiesReader;
import lombok.SneakyThrows;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class UserRepoImpl implements UserRepository{

    private User userRowMapping(ResultSet rs) throws SQLException {

        return User.builder()
                .id(rs.getLong(UserTableColumns.ID))
                .firstName(rs.getString(UserTableColumns.NAME))
                .lastName(rs.getString(UserTableColumns.SURNAME))
                .email(rs.getString(UserTableColumns.EMAIL))
                .creationDate(rs.getTimestamp(UserTableColumns.CREATED))
                .modificationDate(rs.getTimestamp(UserTableColumns.CHANGED))
                .isDeleted(rs.getBoolean(UserTableColumns.IS_DELETED))
                .password(rs.getString(UserTableColumns.PASSWORD))
                .roleId(rs.getLong(UserTableColumns.ROLE_ID))
                .build();
    }


    @Override
    public User findById(Long id) {
        final String findByIdQuery = "select * from testjfx.users where id = " + id+" and is_deleted=false";

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findByIdQuery);
            boolean hasRow = rs.next();
            if (hasRow) {
                return userRowMapping(rs);
            } else {
                throw new NoSuchEntityException("Entity User with id " + id + " does not exist", 404);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public Optional<User> findOne(Long id) {
         return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return findAll(Optional.of(DEFAULT_FIND_ALL_LIMIT), DEFAULT_FIND_ALL_OFFSET);
    }

    @Override
    public List<User> findAll(Optional<Integer> limit, int offset) {
        final String findAllQuery = "select * from testjfx.users " + "where is_deleted=false order by id limit " + limit + " offset " + offset;

        List<User> result = new ArrayList<>();

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findAllQuery);

            while (rs.next()) {
                result.add(userRowMapping(rs));
            }

            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @SneakyThrows
    @Override
    public User create(User object) {
        final String insertQuery =
                "insert into testjfx.users (first_name, last_name, email , creation_date, modification_date, is_deleted, password, role_id) " +
                        " values (?, ?, ?, ?, ?, ?, ?,?);";

        Connection connection;
        PreparedStatement statement;
        PasswordEncoder encoder=new BCryptPasswordEncoder();

        try {

            connection = DBPropertiesReader.getConnection();
            statement = connection.prepareStatement(insertQuery);

            statement.setString(1, object.getFirstName());
            statement.setString(2, object.getLastName());
            statement.setString(3, object.getEmail());
            statement.setTimestamp(4, new Timestamp(new Date().getTime()));
            statement.setTimestamp(5, null);
            statement.setBoolean(6,false);
            statement.setString(7, encoder.encode(object.getPassword()));
            statement.setLong(8,1);
            //executeUpdate - for INSERT, UPDATE, DELETE
            statement.executeUpdate();
            //For select
            //statement.executeQuery();

            /*Get users last insert id for finding new object in DB*/
            ResultSet resultSet = connection.prepareStatement("SELECT currval('testjfx.users_id_seq') as last_id").executeQuery();
            resultSet.next();
            long userLastInsertId = resultSet.getLong("last_id");

            return findById(userLastInsertId);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RecurringEmailException("User with this email already exist!", 409);
        }
    }

    @Override
    public User update(User object) {
        return null;
    }

    @Override
    public Long delete(Long id) {
        return null;
    }

    @Override
    public User findByEmail(String email) {
        final String findByEmailQuery = "select * from testjfx.users where email = '" + email+"'and is_deleted = false";

        Connection connection;
        Statement statement;
        ResultSet rs;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(findByEmailQuery);
            boolean hasRow = rs.next();
            if (hasRow) {
                return userRowMapping(rs);
            } else {
                throw new NoSuchEntityException("User with this email does not exist", 404);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public User updatePassword(Long id, String password) {
        final String updateQuery =
                "update testjfx.users " +
                        "set " +
                        "password = ?, modification_date = ?" +
                        " where id = ?";

        Connection connection;
        PreparedStatement statement;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.prepareStatement(updateQuery);

            statement.setString(1, password);
            statement.setLong(3, id);
            statement.setTimestamp(2, new Timestamp(new Date().getTime()));

            statement.executeUpdate();

            return findById(id);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public User updateNamesAndEmail(Long id, String firstName, String lastName, String email) {
        final String updateQuery =
                "update testjfx.users " +
                        "set " +
                        "first_name = ? , last_name = ?, email = ?, modification_date = ?" +
                        " where id = ?";

        Connection connection;
        PreparedStatement statement;

        try {
            connection = DBPropertiesReader.getConnection();
            statement = connection.prepareStatement(updateQuery);

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setLong(5, id);
            statement.setTimestamp(4, new Timestamp(new Date().getTime()));

            statement.executeUpdate();

            return findById(id);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("SQL Issues!");
        }
    }

    @Override
    public void close() throws Exception {

    }
}
