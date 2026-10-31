package br.com.openprontuario.fila.api.comum;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

/**
 * O Tomcat lê cabeçalhos como ISO-8859-1. Clientes como o Node enviam "João" em Latin-1 (chega
 * certo); outros (curl, proxies) enviam em UTF-8 (chegaria "JoÃ£o"). Se os bytes formarem UTF-8
 * válido, decodifica como UTF-8; senão mantém o valor Latin-1.
 */
public final class CabecalhoTexto {

    private CabecalhoTexto() {
    }

    public static String decodificar(String valor) {
        if (valor == null) {
            return null;
        }
        byte[] bytes = valor.getBytes(StandardCharsets.ISO_8859_1);
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException naoEhUtf8) {
            return valor;
        }
    }
}
