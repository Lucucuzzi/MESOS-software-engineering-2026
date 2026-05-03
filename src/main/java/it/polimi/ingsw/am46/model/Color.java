package it.polimi.ingsw.am46.model;

public enum Color {
    RED("\u001B[91m"),
    PURPLE("\u001B[95m"),
    YELLOW("\u001B[93m"),
    WHITE ("\u001B[97m"),
    BLUE ("\u001B[94m");

    private final String code;

    Color(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
