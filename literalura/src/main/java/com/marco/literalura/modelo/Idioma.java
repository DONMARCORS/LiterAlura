package com.marco.literalura.modelo;

public enum Idioma {
    ES("es"),
    EN("en");

    private String idioma;

    Idioma(String idioma) {
        this.idioma = idioma;
    }

    public String getIdioma(){
        return this.idioma;
    }

    public static Idioma fromString(String text) {
        for (Idioma idioma : Idioma.values()) {
            if (idioma.idioma.equalsIgnoreCase(text)) {
                return idioma;
            }
        }
        throw new IllegalArgumentException("No hay un idioma llamado " + text + ".");
    }
}