package com.pnaelite.estoque.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    Optional<Ingrediente> findByNome(String nome);
}
