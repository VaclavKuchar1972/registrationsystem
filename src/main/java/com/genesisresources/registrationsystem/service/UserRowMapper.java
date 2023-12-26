package com.genesisresources.registrationsystem.service;

import com.genesisresources.registrationsystem.model.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("personID"),
                rs.getString("uuid")
        );
    }

}
