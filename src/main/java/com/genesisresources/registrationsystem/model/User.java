package com.genesisresources.registrationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class User {
    private Long id;
    private String name;
    private String surname;
    private String personID;
    private String uuid;


    public User(long id, String name, String surname, String personID, String uuid) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.personID = personID;
        this.uuid = uuid;
    }

    public User() {}


    public boolean isTooLongName() {return name.length() > 255;}
    public boolean isTooLongSurname() {return surname.length() > 255;}
    public boolean isValidPersonID() {
        return personID != null && personID.length() == 12 && personID.matches("[a-zA-Z0-9]{12}");
    }
    public boolean isValidUUID() {
        String uuidPattern = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
        return uuid != null && Pattern.matches(uuidPattern, uuid);
    }

    public Object getUserNoExtension() {
        return new Object() {
            public String id = String.valueOf(getId());
            public String name = getName();
            public String surname = getSurname();
        };
    }

    public Object getUserWithExtension() {
        return new Object() {
            public String id = String.valueOf(getId());
            public String name = getName();
            public String surname = getSurname();
            public String personID = getPersonID();
            public String uuid = getUuid();
        };
    }


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getSurname() {return surname;}
    public void setSurname(String surname) {this.surname = surname;}
    public String getPersonID() {return personID;}
    public void setPersonID(String personID) {this.personID = personID;}
    public String getUuid() {return uuid;}
    public void setUuid(String uuid) {this.uuid = uuid;}

    @JsonIgnore
    public String getNameSurnamePersonId() {return name + " " + surname + " s personID " + personID;}

    @JsonIgnore
    public String getNameSurname() {return name + " " + surname;}

}