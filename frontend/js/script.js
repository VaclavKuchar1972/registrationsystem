
function hideAllForms() {
  var forms = document.querySelectorAll(".return-form");
  forms.forEach(function (form) {form.style.display = "none";});
}

function clearFormFields(formId) {
  var form = document.getElementById(formId);
  var inputs = form.getElementsByTagName("input");
  for (var i = 0; i < inputs.length; i++) {if (inputs[i].type === "text") {inputs[i].value = "";}}
}

function showRegistrationForm() {
  hideAllForms();
  clearFormFields("registrationForm");
  document.getElementById("registrationForm").style.display = "block";
}

function showEditUserInfoForm() {
  hideAllForms();
  clearFormFields("editUserForm");
  document.getElementById("editUserForm").style.display = "block";
}

function showDeleteUserForm() {
  hideAllForms();
  clearFormFields("deleteUserForm");
  document.getElementById("deleteUserForm").style.display = "block";
}

function showBasicInfoForm() {
  hideAllForms();
  document.getElementById("basicInfoUserForm").style.display = "block";
}

function showExtendedInfoForm() {
  hideAllForms(); document.getElementById("extendedInfoUserForm").style.display = "block";
}


function handleResponse(response) {
  if (!response.ok) {
    return response.text().then(data => {
      throw new Error(`${data}`);
      // Kód pro fyzické testování v prohlížeči, který vrací i číslo chyby
      //throw new Error(`HTTP chyba! Status: ${response.status}. Chybová odpověď: ${data}`);
    });
  }
  return response.text();
}

function setResponseTextAndDisplayBlock(text) {
  document.getElementById("responseText").textContent = text;
  document.getElementById("responseBlock").style.display = "block";
}

function validateUserId(userId) {
    if (!/^[1-9]\d*$/.test(userId)) {
        setResponseTextAndDisplayBlock("Vážený uživateli, Vámi zadané ID má zápornou hodnotu, hodnotu nula, jeho "
        + "hodnota není celočíselná nebo jste ho nevyplnili do příslušného políčka vůbec. Prosíme, opravte to a zkuste "
        + "to znovu. Děkujeme.");
        return false;
    }
    return true;
}

function clearTable(tableId) {
  var tableBody = document.getElementById(tableId);
  while (tableBody.firstChild) {
    tableBody.removeChild(tableBody.firstChild);
  }
}


function submitRegistrationForm() {
  var name = document.getElementById("registration-input-1").value;
  var surname = document.getElementById("registration-input-2").value;
  var personID = document.getElementById("registration-input-3").value;
  var user = {name: name, surname: surname, personID: personID};
  fetch("http://localhost:8080/api/v1/user", {
    method: 'POST', headers: {"Content-Type": "application/json"}, body: JSON.stringify(user)
  })
  .then(handleResponse)
  .then(data => {
    console.log(data);
    hideAllForms();
    clearFormFields("registrationForm");
    setResponseTextAndDisplayBlock(data);
  })
  .catch(err => {
    console.log(err);
    setResponseTextAndDisplayBlock(err.message);
  });
}

function submitEditForm() {
    var userId = document.getElementById("edit-input-1").value;
    var newName = document.getElementById("edit-input-2").value;
    var newSurname = document.getElementById("edit-input-3").value;
    if (!validateUserId(userId)) {return;}
    var userUpdate = {id: userId, name: newName, surname: newSurname};
    fetch("http://localhost:8080/api/v1/user", {
      method: 'PUT', headers: {"Content-Type": "application/json"}, body: JSON.stringify(userUpdate)
    })
    .then(handleResponse)
    .then(data => {
      console.log(data);
      hideAllForms();
      clearFormFields("editUserForm");
      setResponseTextAndDisplayBlock(data);
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}

function submitDeleteForm() {
    var userId = document.getElementById("delete-input-1").value;
    if (!validateUserId(userId)) {return;}
    var userUpdate = {id: userId};
    fetch("http://localhost:8080/api/v1/user", {
      method: 'DELETE', headers: {"Content-Type": "application/json"}, body: JSON.stringify(userUpdate)
    })
    .then(handleResponse)
    .then(data => {
      console.log(data);
      hideAllForms();
      clearFormFields("deleteUserForm");
      setResponseTextAndDisplayBlock(data);
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}

function submitBasicInfoForm() {
  clearTable("resultTableBody");
  var userId = document.getElementById("basic-input-1").value;
  if (!validateUserId(userId)) {return;}
  fetch("http://localhost:8080/api/v1/user/" + userId)
    .then(handleResponse)
    .then(data => {
      console.log(data);
      hideAllForms();
      clearFormFields("basicInfoUserForm");
      var resultTableBody = document.getElementById("resultTableBody");
      var userData = JSON.parse(data);
      var newRow = resultTableBody.insertRow();
      newRow.insertCell(0).textContent = userData.id;
      newRow.insertCell(1).textContent = userData.name;
      newRow.insertCell(2).textContent = userData.surname;
      document.getElementById("resultTable").style.display = "block";
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}

function submitExtendedInfoForm() {
  clearTable("extendedResultTableBody");
  var userId = document.getElementById("extended-input-1").value;
  if (!validateUserId(userId)) {return;}
  fetch("http://localhost:8080/api/v1/user/" + userId + "?detail=true")
    .then(handleResponse)
    .then(data => {
      console.log(data);
      hideAllForms();
      clearFormFields("extendedInfoUserForm");
      var resultTableBody = document.getElementById("extendedResultTableBody");
      var userData = JSON.parse(data);
      var newRow = resultTableBody.insertRow();
      newRow.insertCell(0).textContent = userData.id;
      newRow.insertCell(1).textContent = userData.name;
      newRow.insertCell(2).textContent = userData.surname;
      newRow.insertCell(3).textContent = userData.personID;
      newRow.insertCell(4).textContent = userData.uuid;
      document.getElementById("extendedResultTable").style.display = "block";
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}

function showAllUsersBasicInfo() {
  hideAllForms();
  clearTable("allUsersResultTableBody");
  fetch("http://localhost:8080/api/v1/users")
    .then(handleResponse)
    .then(data => {
      console.log(data);
      var resultTableBody = document.getElementById("allUsersResultTableBody");
      var usersData = JSON.parse(data);
      usersData.forEach(user => {
        var newRow = resultTableBody.insertRow();
        newRow.insertCell(0).textContent = user.id;
        newRow.insertCell(1).textContent = user.name;
        newRow.insertCell(2).textContent = user.surname;
      });
      document.getElementById("allUsersResultTable").style.display = "block";
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}

function showAllUsersExtendedInfo() {
  hideAllForms();
  clearTable("allUsersExtendedResultTableBody");
  fetch("http://localhost:8080/api/v1/users?detail=true")
    .then(handleResponse)
    .then(data => {
      console.log(data);
      var resultTableBody = document.getElementById("allUsersExtendedResultTableBody");
      var usersData = JSON.parse(data);
      usersData.forEach(user => {
        var newRow = resultTableBody.insertRow();
        newRow.insertCell(0).textContent = user.id;
        newRow.insertCell(1).textContent = user.name;
        newRow.insertCell(2).textContent = user.surname;
        newRow.insertCell(3).textContent = user.personID;
        newRow.insertCell(4).textContent = user.uuid;
      });
      document.getElementById("allUsersExtendedResultTable").style.display = "block";
    })
    .catch(err => {
      console.log(err);
      setResponseTextAndDisplayBlock(err.message);
    });
}











