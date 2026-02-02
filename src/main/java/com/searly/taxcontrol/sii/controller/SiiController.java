package com.searly.taxcontrol.sii.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.sii.service.SiiApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/sii")
public class SiiController {

    private final SiiApiService siiApiService;
    private final ObjectMapper objectMapper;

    public SiiController(SiiApiService siiApiService, ObjectMapper objectMapper) {
        this.siiApiService = siiApiService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "发送电子发票",
            responses = {
                    @ApiResponse(responseCode = "200", description = "发送成功", content = @Content(schema = @Schema(implementation = ResultadoEnvioPost.class)))
            }
    )
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

    @Operation(
            summary = "发送日结（RVD）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "发送成功", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping(value = "/rvd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerDailyReport(
            @RequestPart("request") String requestJson,
            @RequestPart("caf") MultipartFile caf
    ) throws Exception {
        RegisterDailyReportRequest req = objectMapper.readValue(requestJson, RegisterDailyReportRequest.class);

        InvoiceSendRequest sendRequest = new InvoiceSendRequest();
        sendRequest.setAliasDocumento(req.aliasDocumento);
        sendRequest.setAliasSetDte(req.aliasSetDte);

        byte[] cafBytes = caf.getBytes();
        try (ByteArrayInputStream is = new ByteArrayInputStream(cafBytes)) {
            sendRequest.setCafFile(is);
            String endpoint = (req.endpointPath == null || req.endpointPath.isBlank()) ? "/boleta.electronica.rvd" : req.endpointPath;
            return siiApiService.registerDailyReport(sendRequest, req.invoices, endpoint);
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

    public static class RegisterDailyReportRequest {
        public List<InvoiceData> invoices;
        public String aliasDocumento;
        public String aliasSetDte;
        public String endpointPath;
    }
}
