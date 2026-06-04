module it.polimi.ingsw.am46 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires java.desktop;
    requires com.google.gson;
    requires java.rmi;
    requires java.sql;

    opens it.polimi.ingsw.am46 to javafx.fxml;
    exports it.polimi.ingsw.am46;
    exports it.polimi.ingsw.am46.server.model;
    opens it.polimi.ingsw.am46.server.model to javafx.fxml;
    exports it.polimi.ingsw.am46.server.model.state;
    opens it.polimi.ingsw.am46.server.model.state to javafx.fxml;
    exports it.polimi.ingsw.am46.server.model.cards;
    exports it.polimi.ingsw.am46.network to java.rmi;
    exports it.polimi.ingsw.am46.network.rmi.server to java.rmi;
    exports it.polimi.ingsw.am46.network.rmi.client to java.rmi;
    exports it.polimi.ingsw.am46.network.dto;
    // We open the 'cards' package to Gson because it uses reflection to access and
    // inject values into the fields of our DTO classes
    // when parsing the JSON file, even if those fields are private.
    opens it.polimi.ingsw.am46.server.model.cards to javafx.fxml, com.google.gson;
    opens it.polimi.ingsw.am46.network.dto to com.google.gson;

    exports it.polimi.ingsw.am46.view.gui to javafx.graphics;
    opens it.polimi.ingsw.am46.view.gui to javafx.graphics;
}