module it.polimi.ingsw.am46 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens it.polimi.ingsw.am46 to javafx.fxml;
    exports it.polimi.ingsw.am46;
    exports it.polimi.ingsw.am46.model;
    opens it.polimi.ingsw.am46.model to javafx.fxml;
}