package com.searly.taxcontrol.sii.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.sii.service.SiiApiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/sii")
public class SiiController {

    private final SiiApiService siiApiService;
    private final ObjectMapper objectMapper;

    public SiiController(SiiApiService siiApiService, ObjectMapper objectMapper) {
        this.siiApiService = siiApiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/invoices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoEnvioPost registerInvoice(
            @RequestPart("request") String requestJson,
            @RequestPart("caf") MultipartFile caf
    ) throws Exception {
        RegisterInvoiceRequest req = objectMapper.readValue(requestJson, RegisterInvoiceRequest.class);

        InvoiceSendRequest sendRequest = new InvoiceSendRequest();
        sendRequest.setRutSender(req.rutSender);
        sendRequest.setDvSender(req.dvSender);
        sendRequest.setRutCompany(req.rutCompany);
        sendRequest.setDvCompany(req.dvCompany);
        sendRequest.setInvoiceData(req.invoiceData);
        sendRequest.setAliasDocumento(req.aliasDocumento);
        sendRequest.setAliasSetDte(req.aliasSetDte);

        byte[] cafBytes = caf.getBytes();
        try (ByteArrayInputStream is = new ByteArrayInputStream(cafBytes)) {
            sendRequest.setCafFile(is);
            return siiApiService.registerInvoice(sendRequest);
        }
    }

    @GetMapping("/invoices/{rut}-{dv}/{trackId}")
    public SiiEnvioStatusResponse queryInvoice(
            @PathVariable("rut") String rut,
            @PathVariable("dv") String dv,
            @PathVariable("trackId") Long trackId
    ) {
        return siiApiService.queryInvoice(rut, dv, trackId);
    }

    public static class RegisterInvoiceRequest {
        public String rutSender;
        public String dvSender;
        public String rutCompany;
        public String dvCompany;
        public InvoiceData invoiceData;
        public String aliasDocumento;
        public String aliasSetDte;
    }
}
