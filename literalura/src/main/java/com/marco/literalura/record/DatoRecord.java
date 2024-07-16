package com.marco.literalura.record;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatoRecord(
        @JsonAlias("count") Integer total,
        @JsonAlias("results") List<LibroRecord> libros) {
}
