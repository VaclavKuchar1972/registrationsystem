package com.genesisresources.registrationsystem.service;

import com.genesisresources.registrationsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper = new UserRowMapper();

    @Autowired
    public UserService(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}
    public boolean isPersonIDUsed(String personID) {
        String sql = "SELECT COUNT(*) FROM register WHERE PersonID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, personID);
        return count != null && count > 0;
    }
    public User addUser(User user) {
        final String sql = "INSERT INTO register (Name, Surname, PersonID, Uuid) VALUES (?, ?, ?, ?)";

        // Automatické nastavení ID uživatele získaného z databáze - poznámka pro mě
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getSurname());
            ps.setString(3, user.getPersonID());
            ps.setString(4, UUID.randomUUID().toString()); // toto je JAVOVSKÝ generátor UUID
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {user.setId(key.longValue());}

        return user;
    }
    public User findUserById(Long id) {
        String sql = "SELECT id, name, surname, personID, uuid FROM register WHERE id = ?";
        try {return jdbcTemplate.queryForObject(sql, new Object[]{id}, userRowMapper);}
        catch (EmptyResultDataAccessException e) {return null;}
    }
    public List<User> findAllUsers() {
        String sql = "SELECT id, name, surname, personID, uuid FROM register";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    public void updateUser(User user) {
        String sql = "UPDATE register SET Name = ?, Surname = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), user.getId());
    }
    public void deleteUser(Long id) {
        String sql = "DELETE FROM register WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

}