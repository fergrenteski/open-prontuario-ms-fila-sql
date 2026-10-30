package br.com.openprontuario.fila.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Sintomas gravados como array JSON em NVARCHAR(MAX): são sempre lidos/escritos junto do registro. */
@Converter
class SintomasJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<String>> LISTA = new TypeReference<>() { };

    @Override
    public String convertToDatabaseColumn(List<String> sintomas) {
        try {
            return JSON.writeValueAsString(sintomas);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar sintomas", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String coluna) {
        try {
            return JSON.readValue(coluna, LISTA);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Coluna sintomas com JSON inválido", e);
        }
    }
}
