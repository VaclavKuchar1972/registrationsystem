package com.genesisresources.registrationsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PersonIDService {

    private List<String> validPersonIDsList;
    private final Path filePath;
    private static final Logger LOGGER = Logger.getLogger(PersonIDService.class.getName());

    public boolean isValidPersonID(String personID) {return validPersonIDsList.contains(personID);}

    public PersonIDService(@Value("${personid.filepath}") String filename) {
        this.validPersonIDsList = new ArrayList<>();
        this.filePath = Paths.get(filename);
        loadValidPersonIDsList();
    }

    private void loadValidPersonIDsList() {
        try {
            validPersonIDsList = Files.readAllLines(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Chyba! Nelze načíst soubor s platnými personID.", e);
        }
    }

    public boolean removePersonID(String personID) {
        boolean removed = validPersonIDsList.remove(personID);
        if (removed) {
            try {
                Files.write(
                        filePath, validPersonIDsList, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
                LOGGER.log(
                        Level.SEVERE, "Chyba! Nelze aktualizovat soubor s platnými personID po odstranění ID.", e
                );
                return false;
            }
        }
        return removed;
    }

    public List<String> getValidPersonIDsList() {return validPersonIDsList;}

}