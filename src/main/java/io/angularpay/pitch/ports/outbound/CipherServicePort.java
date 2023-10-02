package io.angularpay.pitch.ports.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pitch.models.VerifySignatureResponseModel;

import java.util.Map;

public interface CipherServicePort {
    VerifySignatureResponseModel verifySignature(String requestBody, Map<String, String> headers) throws JsonProcessingException;
}
