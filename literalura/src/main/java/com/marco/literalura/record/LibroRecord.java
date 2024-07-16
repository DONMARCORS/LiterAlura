package com.marco.literalura.record;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LibroRecord(
    @JsonAlias("id") Long id,
    @JsonAlias("title") String titulo,
    @JsonAlias("authors") List<AutorRecord> autores,
    @JsonAlias("languages") List<String> idiomas,
    @JsonAlias("copyright") String copyright,
    @JsonAlias("download_count") Integer descargas) {
}