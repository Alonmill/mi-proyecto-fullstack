package com.example.practica.Controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica.Service.ReportesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportesController {

    private final ReportesService reportesService;

    @GetMapping(value = "/citas", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteCitas() {
        return pdf("reporte-citas.pdf", reportesService.reporteCitasPdf());
    }

    @GetMapping(value = "/recetas", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteRecetas() {
        return pdf("reporte-recetas.pdf", reportesService.reporteRecetasPdf());
    }

    @GetMapping(value = "/expedientes", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteExpedientes() {
        return pdf("reporte-expedientes.pdf", reportesService.reporteExpedientesPdf());
    }

    @GetMapping(value = "/pacientes", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reportePacientes() {
        return pdf("reporte-pacientes.pdf", reportesService.reportePacientesPdf());
    }

    @GetMapping(value = "/medicos", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteMedicos() {
        return pdf("reporte-medicos.pdf", reportesService.reporteMedicosPdf());
    }

    @GetMapping(value = "/medicamentos", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteMedicamentos() {
        return pdf("reporte-medicamentos.pdf", reportesService.reporteMedicamentosPdf());
    }

    private ResponseEntity<byte[]> pdf(String filename, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
