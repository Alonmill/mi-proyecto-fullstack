package com.example.practica.Controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica.DTO.ReporteTablaDTO;
import com.example.practica.Service.ReportesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportesController {

    private final ReportesService reportesService;

    @GetMapping("/citas/data")
    public ResponseEntity<ReporteTablaDTO> dataCitas() { return ResponseEntity.ok(reportesService.dataCitas()); }

    @GetMapping(value = "/citas", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteCitas() { return pdf("reporte-citas.pdf", reportesService.reporteCitasPdf()); }

    @GetMapping("/recetas/data")
    public ResponseEntity<ReporteTablaDTO> dataRecetas() { return ResponseEntity.ok(reportesService.dataRecetas()); }

    @GetMapping(value = "/recetas", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteRecetas() { return pdf("reporte-recetas.pdf", reportesService.reporteRecetasPdf()); }

    @GetMapping("/expedientes/data")
    public ResponseEntity<ReporteTablaDTO> dataExpedientes() { return ResponseEntity.ok(reportesService.dataExpedientes()); }

    @GetMapping(value = "/expedientes", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteExpedientes() { return pdf("reporte-expedientes.pdf", reportesService.reporteExpedientesPdf()); }

    @GetMapping("/pacientes/data")
    public ResponseEntity<ReporteTablaDTO> dataPacientes() { return ResponseEntity.ok(reportesService.dataPacientes()); }

    @GetMapping(value = "/pacientes", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reportePacientes() { return pdf("reporte-pacientes.pdf", reportesService.reportePacientesPdf()); }

    @GetMapping("/pacientes-por-dia/data")
    public ResponseEntity<ReporteTablaDTO> dataPacientesPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reportesService.dataPacientesPorDia(fecha));
    }

    @GetMapping(value = "/pacientes-por-dia", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reportePacientesPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return pdf("reporte-pacientes-por-dia.pdf", reportesService.reportePacientesPorDiaPdf(fecha));
    }

    @GetMapping("/medicos/data")
    public ResponseEntity<ReporteTablaDTO> dataMedicos() { return ResponseEntity.ok(reportesService.dataMedicos()); }

    @GetMapping(value = "/medicos", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteMedicos() { return pdf("reporte-medicos.pdf", reportesService.reporteMedicosPdf()); }

    @GetMapping("/medicamentos/data")
    public ResponseEntity<ReporteTablaDTO> dataMedicamentos() { return ResponseEntity.ok(reportesService.dataMedicamentos()); }

    @GetMapping(value = "/medicamentos", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reporteMedicamentos() { return pdf("reporte-medicamentos.pdf", reportesService.reporteMedicamentosPdf()); }

    private ResponseEntity<byte[]> pdf(String filename, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
