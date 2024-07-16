package com.marco.literalura;

import java.util.*;
import java.util.stream.Collectors;

import com.marco.literalura.modelo.Autor;
import com.marco.literalura.modelo.Idioma;
import com.marco.literalura.modelo.Libro;
import com.marco.literalura.record.DatoRecord;
import com.marco.literalura.record.LibroRecord;
import com.marco.literalura.repo.AutorRepo;

public class Main {
    private Scanner teclado = new Scanner(System.in);
    private ConexionAPI conexionAPI = new ConexionAPI();
    private Mapeador conversor = new Mapeador();
    private String URL_BASE = "https://gutendex.com/books/";
    private AutorRepo repository;

    public Main (AutorRepo repository) {
        this.repository = repository;
    }

    public void mostrarMenu() {
        var opcion = -1;
        var menu = """
                ============================================
                LITERALURA
                --------------------------------------------
                1) Buscar Libros por TÍtulo
                2) Buscar Autor por Nombre
                3) Listar Libros Registrados
                4) Listar Autores Registrados
                5) Listar Autores Vivos
                6) Listar Libros por Idioma
                7) Listar Autores por Año
                8) Top 10 Libros más Buscados
                9) Generar Estadísticas
                0) Salir
                ----------------------------------------------
                Elija una opción por favor:
                ==============================================
                """;

        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        buscarAutorPorNombre();
                        break;
                    case 3:
                        listarLibrosRegistrados();
                        break;
                    case 4:
                        listarAutoresRegistrados();
                        break;
                    case 5:
                        listarAutoresVivos();
                        break;
                    case 6:
                        listarLibrosPorIdioma();
                        break;
                    case 7:
                        listarAutoresPorAnio();
                        break;
                    case 8:
                        top10Libros();
                        break;
                    case 9:
                        generarEstadisticas();
                        break;
                    case 0:
                        System.out.println("Nos vemoos!");
                        break;
                    default:
                        System.out.println("Introduce una opcion correcta porfavor");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Introduce una opcion de manera correcta");

            }
        }
    }

    public void buscarLibroPorTitulo() {
        System.out.println("""
                ================================
                BUSCAR LIBROS POR TÍTULO
                ================================
                 """);
        System.out.println("Introduce el nombre del libro que quieres buscar:");
        var nombre = teclado.nextLine();
        var json = conexionAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

        if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            var datos = conversor.obtenerDatos(json, DatoRecord.class);

            Optional<LibroRecord> libroBuscado = datos.libros().stream()
                    .findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println(
                        "===================================================\n" +
                        "LIBRO" +
                                "\nTítulo: " + libroBuscado.get().titulo() +
                                "\nAutor: " + libroBuscado.get().autores().stream()
                                        .map(a -> a.nombre()).limit(1).collect(Collectors.joining())
                                +
                                "\nNúmero de descargas: " + libroBuscado.get().descargas() +
                                "\nIdioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                                "\n============================================\n");

                try {
                    List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a))
                            .collect(Collectors.toList());
                    Autor autorAPI = libroBuscado.stream().flatMap(l -> l.autores().stream()
                            .map(a -> new Autor(a)))
                            .collect(Collectors.toList()).stream().findFirst().get();
                    Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                            .map(a -> a.nombre())
                            .collect(Collectors.joining()));
                    Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
                    if (libroOptional.isPresent()) {
                        System.out.println("El libro ya está guardado en la Base de datos.");
                    } else {
                        Autor autor;
                        if (autorBD.isPresent()) {
                            autor = autorBD.get();
                            System.out.println("El autor ya esta guardado en la Base de datos");
                        } else {
                            autor = autorAPI;
                            repository.save(autor);
                        }
                        autor.setLibros(libroEncontrado);
                        repository.save(autor);
                    }
                } catch (Exception e) {
                    System.out.println("Advertencia: " + e.getMessage());
                }
            } else {
                System.out.println("El libro no fue encontrado");
            }
        }
    }

    public void buscarAutorPorNombre() {
        System.out.println("""
                ===============================
                BUSCAR AUTOR POR NOMBRE
                ===============================
                """);
        System.out.println("Introdcue el nombre del autor que quieres buscar:");
        var nombre = teclado.nextLine();
        Optional<Autor> autor = repository.buscarAutorPorNombre(nombre);
        if (autor.isPresent()) {
            System.out.println(
                    "\nAutor: " + autor.get().getNombre() +
                            "\nFecha de Nacimiento: " + autor.get().getNacimiento() +
                            "\nFecha de Fallecimiento: " + autor.get().getFallecimiento() +
                            "\nLibros: " + autor.get().getLibros().stream()
                                    .map(l -> l.getTitulo()).collect(Collectors.toList())
                            + "\n");
        } else {
            System.out.println("El autor no existe en la Base de datos");
        }
    }

    public void listarLibrosRegistrados() {
        System.out.println("""
                ==================================
                MOSTRAR LIBROS GUARDADOS
                ==================================
                 """);
        List<Libro> libros = repository.buscarTodosLosLibros();
        libros.forEach(l -> System.out.println(
                "====================================================\n" +
                "LIBRO" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutor().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getDescargas() +
                        "\n=============================================\n"));
    }

    public void listarAutoresRegistrados() {
        System.out.println("""
                ==================================
                MOSTRAR AUTORES REGISTRADOS
                ==================================
                 """);
        List<Autor> autores = repository.findAll();
        System.out.println();
        autores.forEach(l -> System.out.println(
                "Autor: " + l.getNombre() +
                        "\nFecha de Nacimiento: " + l.getNacimiento() +
                        "\nFecha de Fallecimiento: " + l.getFallecimiento() +
                        "\nLibros: " + l.getLibros().stream()
                                .map(t -> t.getTitulo()).collect(Collectors.toList())
                        + "\n"));
    }

    public void listarAutoresVivos() {
        System.out.println("""
                ==================================
                MOSTRAR AUTORES VIVO
                ==================================
                 """);
        System.out.println("Introduzca un año para verificar el autor que desea buscar:");
        try {
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repository.buscarAutoresVivos(fecha);
            if (!autores.isEmpty()) {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getNacimiento() +
                                "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                                "\nLibros: " + a.getLibros().stream()
                                        .map(l -> l.getTitulo()).collect(Collectors.toList())
                                + "\n"));
            } else {
                System.out.println("No hay autores vivos en ese año.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un año válido ");
        }
    }

    public void listarLibrosPorIdioma() {
        System.out.println("""
                ==================================
                MOSTRAR LIBROS POR IDIOMA
                ==================================
                """);
        var menu = """
                Seleccione el idioma del libro que desea encontrar:
                ---------------------------------------------------
                1) Español
                2) Inglés
                ----------------------------------------------------
                """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1:
                    buscarLibrosPorIdioma("es");
                    break;
                case 2:
                    buscarLibrosPorIdioma("en");
                    break;
                default:
                    System.out.println("Opción inválida!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida");
        }
    }

    private void buscarLibrosPorIdioma(String idioma) {
        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repository.buscarLibrosPorIdioma(idiomaEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros guardados en ese idioma");
            } else {
                System.out.println();
                libros.forEach(l -> System.out.println(
                        "==========================================\n" +
                        "LIBRO" +
                                "\nTítulo: " + l.getTitulo() +
                                "\nAutor: " + l.getAutor().getNombre() +
                                "\nIdioma: " + l.getIdioma().getIdioma() +
                                "\nNúmero de descargas: " + l.getDescargas() +
                                "\n==========================================n"));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Introduce un idioma válido en el formato especificado.");
        }
    }

    public void listarAutoresPorAnio() {
        System.out.println("""
                ==================================
                MOSTRAR AUTORES POR AÑO
                ==================================
                 """);
        var menu = """
                Ingresa una opción para listar los autores
                -------------------------------------------
                1) Listar autor por Año de Nacimiento
                2) Listar autor por año de Fallecimiento
                -------------------------------------------
                """;
        System.out.println(menu);
        try {
            var opcion = Integer.valueOf(teclado.nextLine());
            switch (opcion) {
                case 1:
                    ListarAutoresPorNacimiento();
                    break;
                case 2:
                    ListarAutoresPorFallecimiento();
                    break;
                default:
                    System.out.println("Opción inválida!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    public void ListarAutoresPorNacimiento() {
        System.out.println("""
                ==========================================
                BUSCAR AUTOR POR SU AÑO DE NACIMIENTO
                ==========================================
                """);
        System.out.println("Introduzca el año de nacimiento del autor que desea buscar:");
        try {
            var nacimiento = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repository.listarAutoresPorNacimiento(nacimiento);
            if (autores.isEmpty()) {
                System.out.println("No existen autores con año de nacimiento igual a " + nacimiento);
            } else {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getNacimiento() +
                                "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                                "\nLibros: "
                                + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"));
            }
        } catch (NumberFormatException e) {
            System.out.println("Año no válido: " + e.getMessage());
        }
    }

    public void ListarAutoresPorFallecimiento() {
        System.out.println("""
                =================================================
                BUSCAR LIBROS POR AÑO DE FALLECIMIENTO DEL AUTOR
                =================================================
                 """);
        System.out.println("Introduzca el año de fallecimiento del autor que desea buscar:");
        try {
            var fallecimiento = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repository.listarAutoresPorFallecimiento(fallecimiento);
            if (autores.isEmpty()) {
                System.out.println("No existen autores con año de fallecimiento igual a " + fallecimiento);
            } else {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getNacimiento() +
                                "\nFecha de Fallecimeinto: " + a.getFallecimiento() +
                                "\nLibros: "
                                + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"));
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    public void top10Libros() {
        System.out.println("""
                ==========================================
                TOP 10 LIBROS MÁS BUSCADOS
                ==========================================
                 """);
        List<Libro> libros = repository.top10Libros();
        System.out.println();
        libros.forEach(l -> System.out.println(
                "==========================================\n" +
                "LIBRO" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutor().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getDescargas() +
                        "\n==========================================\n"));
    }

    public void generarEstadisticas() {
        System.out.println("""
                =============================
                GENERAR ESTADÍSTICAS
                =============================
                 """);
        var json = conexionAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, DatoRecord.class);
        IntSummaryStatistics est = datos.libros().stream()
                .filter(l -> l.descargas() > 0)
                .collect(Collectors.summarizingInt(LibroRecord::descargas));
        Integer media = (int) est.getAverage();
        System.out.println("\nESTADÍSTICAS");
        System.out.println("Media de descargas: " + media);
        System.out.println("Máxima de descargas: " + est.getMax());
        System.out.println("Mínima de descargas: " + est.getMin());
        System.out.println("Total registros para calcular las estadísticas: " + est.getCount());
        System.out.println("==========================================\n");
    }
}