package com.genesisresources.registrationsystem.controller;

import com.genesisresources.registrationsystem.model.User;
import com.genesisresources.registrationsystem.service.UserService;
import com.genesisresources.registrationsystem.service.PersonIDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*") // Jen pro fyzické testy front-endu, jinak je to špatně kvůli bezpečnosti kolem CORS
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private String repeatingDBerrMessage = "databáze společnosti Genesis Resources. Prosíme vyčkejte 10 minut a zkuste "
            + "to znovu. Děkujeme Vám za pochopení v této těžké době.";
    private String repeatingDBerrUserMessage = "Omlouváme se, ale vyskytla se chyba při získávání informací o uživatel";

    private final UserService userService;

    private final PersonIDService personIDService;

    @Autowired
    public UserController(UserService userService, PersonIDService personIDService) {
        this.userService = userService;
        this.personIDService = personIDService;
    }


    @PostMapping("/user")
    public ResponseEntity<String> addUser(@RequestBody User newUser) {return commonMethodForAddingUser(newUser);}
    @GetMapping("/user/add")
    public ResponseEntity<String> addUserFromUrl(
            @RequestParam String name, @RequestParam String surname, @RequestParam String personID
    )
    {
        User newUser = new User(); newUser.setName(name); newUser.setSurname(surname); newUser.setPersonID(personID);
        return commonMethodForAddingUser(newUser);
        // Testovací URL:
        //http://localhost:8080/api/v1/user/add?name=Václav&surname=Kuchař&personID=bG2zC7jR9xVp
    }
    private ResponseEntity<String> commonMethodForAddingUser(User newUserRequest) {
        String repeatingLongNSerrMessage = ". Nový uživatel nebyl přidán do databáze společnosti Genesis Resources,"
                + " protože maximální počet znaků pro ";
        String repeatingNullValueNewUserErrMessage = "Nový uživatel nebyl přidán do databáze společnosti Genesis "
                + "Resources. Ale nezoufejte a zkuste to znovu.";
        try {
            if (newUserRequest.getName() == null || newUserRequest.getName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nezadali jste žádné jméno. "
                        + repeatingNullValueNewUserErrMessage);
            }
            if (newUserRequest.isTooLongName()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vážený uživateli, zadal/a jste příliš dlouhé"
                        + " jméno: " + newUserRequest.getName() + repeatingLongNSerrMessage + "jméno je 255. Ale " +
                        "nezoufejte, jméno jen zkraťte a zkuste to znovu.");
            }
            if (newUserRequest.getSurname() == null || newUserRequest.getSurname().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nezadali jste žádné příjmení. "
                        + repeatingNullValueNewUserErrMessage);
            }
            if (newUserRequest.isTooLongSurname()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zadali jste příliš dlouhé příjmení: "
                        + newUserRequest.getSurname() + repeatingLongNSerrMessage + "příjmení je 255. Ale nezoufejte, "
                        + "příjmení jen zkraťte a zkuste to znovu.");
            }
            if (newUserRequest.getPersonID() == null || newUserRequest.getPersonID().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nezadali jste žádné PersonID"
                        + repeatingNullValueNewUserErrMessage);
            }
            if (!newUserRequest.isValidPersonID()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vaše PersonID nemá správný formát. "
                        + "Je nám líto, ale uživatele " + newUserRequest.getNameSurnamePersonId() + " nelze přidat "
                        + "do databáze společnosti Genesis Resources. Nový uživatel nebyl přidán.");
            }
            if (userService.isPersonIDUsed(newUserRequest.getPersonID())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Uživatel s personID "
                        + newUserRequest.getPersonID() + " již existuje v databázi společnosti Genesis Resources. Je "
                        + "nám líto, ale nový uživatel " + newUserRequest.getNameSurnamePersonId() + " nebyl/a přidán "
                        + "do naší databáze.");
            }
            User addedUser = userService.addUser(newUserRequest);
            // Tady jsem oproti pravidlům ponechal zakomentovanou část kódu, nejdříve mi přišlo, že by se person ID mělo
            // odstranit i z toho TXT, když se přidá uživatel, ale nakonec jsem si to rozmyslel, tak kdyby to bylo
            // špatně, jen to odkomentuji. Nepoužitou metodu removePersonID jsem v PersonIDService nechal aktivní.
            //personIDService.removePersonID(addedUser.getPersonID());
            return ResponseEntity.status(HttpStatus.CREATED).body("Gratulujeme, uživatel "
                    + newUserRequest.getNameSurnamePersonId() + " byl/a úspěšně přidán/a do databáze společnosti "
                    + "Genesis Resources.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Omlouváme se, vyskytla se chyba "
                    + "při práci se serverem, uživatel " + newUserRequest.getNameSurnamePersonId() + " nebyl přidán "
                    + "do " + repeatingDBerrMessage);
        }
    }


    @PutMapping("/user")
    public ResponseEntity<String> updateUser(@RequestBody User updatedUser) {
        return commonMethodForUpdateUser(updatedUser.getId(), updatedUser.getName(), updatedUser.getSurname());
    }
    @GetMapping("/user/update")
    public ResponseEntity<String> updateUserFromUrl(
            @RequestParam Long id,
            @RequestParam String newName,
            @RequestParam String newSurname
            // Testovací URL:
            //http://localhost:8080/api/v1/user/update?id=1&newName=Ilona&newSurname=Kuchařová
    ) {return commonMethodForUpdateUser(id, newName, newSurname);}
    private ResponseEntity<String> commonMethodForUpdateUser(Long id, String newName, String newSurname) {
        String repeatingUserUpdateMessage = "Údaje o uživateli v databázi společnosti Genesis Resources tedy nemohli "
                + "být změněny, ale nezoufejte, třeba jste se jenom překlepl/a. Zkuste to znovu.";
        try {
            User existingUser = userService.findUserById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vážený uživateli, Vámi zadané ID " + id
                        + " nebylo nalezeno v databázi společnosti Genesis Resources. "
                        + repeatingUserUpdateMessage);
            }
            if (newName == null || newName == "") {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vážený uživateli, nezadal/a jste žádné nové jméno. " + repeatingUserUpdateMessage);
            } else if (existingUser.isTooLongName()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vážený uživateli, zadal/a jste příliš dlouhé nové jméno. " + repeatingUserUpdateMessage
                                + " Maximální počet znaků pro jméno je 255.");
            }
            if (newSurname == null || newSurname == "") {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vážený uživateli, nezadal/a jste žádné nové příjmení. " + repeatingUserUpdateMessage);
            } else if (existingUser.isTooLongSurname()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vážený uživateli, zadal/a jste příliš dlouhé nové příjmení. "
                                + repeatingUserUpdateMessage + " Maximální počet znaků pro příjmení je 255.");
            }
            String oldNameSurname = existingUser.getNameSurname();
            existingUser.setName(newName);
            existingUser.setSurname(newSurname);
            userService.updateUser(existingUser);
            return ResponseEntity.ok("Gratulujeme. Údaje pro uživatele s ID: " + id + " byly v databázi "
                    + "společnosti Genesis Resources změněny ze starého jména a příjmení " + oldNameSurname + " na nové"
                    + " jméno a příjmení " + existingUser.getNameSurname() + ".");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Omlouváme se, ale vyskytla se chyba při pokusu o aktualizaci údajů uživatele "
                            + repeatingDBerrMessage + " Jedná se o uživatel s číslem ID " + id + ".");
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody User user) {return commonMethodForDeleteUser(user.getId());}
    @GetMapping("/user/delete")
    // Testovací URL:
    //http://localhost:8080/api/v1/user/delete?id=1
    public ResponseEntity<String> deleteUserFromUrl(@RequestParam Long id) {return commonMethodForDeleteUser(id);}
    private ResponseEntity<String> commonMethodForDeleteUser(Long id) {
        try {
            User existingUser = userService.findUserById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Vážený uživateli, Vámi zadané ID " + id + " nebylo nalezeno v databázi společnosti "
                                + "Genesis Resources. Uživatel tedy nebyl smazán. Ale nezoufejte, třeba jste se jen "
                                + "překlepl/a a zkuste top znovu.");
            }
            userService.deleteUser(id);
            return ResponseEntity.ok("Uživatel s ID " + id + " a jménem a příjmením "
                    + existingUser.getNameSurname() + " byl úspěšně smazán/a.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Omlouváme se, ale vyskytla se chyba při pokusu o smazání uživatele z "
                            + repeatingDBerrMessage);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean detail) {
        try {
            User user = userService.findUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Je nám líto, ale uživatel s ID " + id + " nebyl nalezen v databázi společnosti Genesis "
                                + "Resources. Nelze tedy zobrazit jeho údaje.");
            }
            if (detail) {return ResponseEntity.ok(user.getUserWithExtension());}
            // Testovací URL:
            //http://localhost:8080/api/v1/user/1?detail=true
            else {return ResponseEntity.ok(user.getUserNoExtension());}
            // Testovací URL:
            //http://localhost:8080/api/v1/user/1
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(repeatingDBerrUserMessage + "i s ID " + id + " z " + repeatingDBerrMessage);
        }
    }


    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "false") boolean detail) {
        try {
            List<User> users = userService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Žádní uživatelé v databázi společnosti Genesis"
                        + " Resources zatím nejsou. Nemůžeme Vám tedy nic zobrazit.");
            }
            List<Object> formattedUsers = users.stream()
                    .map(user -> detail
                            ? user.getUserWithExtension()
                            // Testovací URL:
                            //http://localhost:8080/api/v1/users?detail=true
                            : user.getUserNoExtension())
                    // Testovací URL:
                    //http://localhost:8080/api/v1/users
                    .collect(Collectors.toList());
            return ResponseEntity.ok(formattedUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(repeatingDBerrUserMessage + "ích. " + repeatingDBerrMessage);
        }
    }


}
