package com.marco.literalura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.marco.literalura.repo.AutorRepo;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner{

	@Autowired
	private AutorRepo autorRepo;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Main main = new Main(autorRepo);
		main.mostrarMenu();
	}
}
