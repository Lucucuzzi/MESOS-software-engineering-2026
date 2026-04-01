module it.polimi.ingsw.am46 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires java.desktop;
    requires com.google.gson;

    opens it.polimi.ingsw.am46 to javafx.fxml;
    exports it.polimi.ingsw.am46;
    exports it.polimi.ingsw.am46.model;
    opens it.polimi.ingsw.am46.model to javafx.fxml;
    exports it.polimi.ingsw.am46.model.state;
    opens it.polimi.ingsw.am46.model.state to javafx.fxml;
    exports it.polimi.ingsw.am46.model.cards;
    // We open the 'cards' package to Gson because it uses reflection to access and
    // inject values into the fields of our DTO classes
    // when parsing the JSON file, even if those fields are private.
    opens it.polimi.ingsw.am46.model.cards to javafx.fxml, com.google.gson;

}