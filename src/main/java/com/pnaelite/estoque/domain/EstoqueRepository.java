package com.pnaelite.estoque.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByEscolaIdAndIngredienteId(Long escolaId, Long ingredienteId);
}
