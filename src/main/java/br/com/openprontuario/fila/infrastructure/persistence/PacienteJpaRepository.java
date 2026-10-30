package br.com.openprontuario.fila.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PacienteJpaRepository extends JpaRepository<PacienteEntity, String> {
}
