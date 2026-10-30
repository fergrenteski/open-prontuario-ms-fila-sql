package br.com.openprontuario.fila.api.fila;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AvisoChegadaRequest(
        @NotNull @Size(min = 1, max = 20) List<@NotBlank @Size(min = 2, max = 120) String> sintomas,
        @Min(0) @Max(600) Integer tempoEstimadoChegadaMin) {
}
