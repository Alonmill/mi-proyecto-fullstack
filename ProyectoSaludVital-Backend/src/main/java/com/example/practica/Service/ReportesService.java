package com.example.practica.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.practica.Model.Cita;
import com.example.practica.Model.EntradaHistorial;
import com.example.practica.Model.ExpedienteMedico;
import com.example.practica.Model.Medicamento;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Receta;
import com.example.practica.Repository.CitaRepository;
import com.example.practica.Repository.ExpedienteMedicoRepository;
import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.RecetaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportesService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CitaRepository citaRepository;
    private final RecetaRepository recetaRepository;
    private final ExpedienteMedicoRepository expedienteRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final MedicamentoRepository medicamentoRepository;

    public byte[] reporteCitasPdf() {
        List<Cita> citas = citaRepository.findAll().stream()
                .sorted(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
                .toList();

        return crearDocumento("Reporte de Citas", new String[] { "Fecha", "Hora", "Paciente", "Médico", "Motivo",
                "Estado", "Tarifa Médico", "Monto Consulta" }, citas.stream().map(c -> new String[] {
                        safeDate(c.getFecha()),
                        c.getHora() != null ? c.getHora().toString() : "-",
                        c.getPaciente() != null ? c.getPaciente().getNombre() : "-",
                        nombreCompleto(c.getMedico()),
                        safe(c.getMotivo()),
                        c.getEstado() != null ? c.getEstado().name() : "-",
                        money(c.getMedico() != null ? c.getMedico().getTarifaConsulta() : null),
                        money(c.getTarifaAplicada())
                }).toList());
    }

    public byte[] reporteRecetasPdf() {
        List<Receta> recetas = recetaRepository.findAll().stream()
                .sorted(Comparator.comparing(Receta::getFechaEmision, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return crearDocumento("Reporte de Recetas", new String[] { "ID", "Paciente", "Médico", "Estado", "Emisión",
                "Caducidad", "Medicamentos" }, recetas.stream().map(r -> new String[] {
                        String.valueOf(r.getId()),
                        r.getPaciente() != null ? r.getPaciente().getNombre() : "-",
                        nombreCompleto(r.getMedico()),
                        r.getEstado() != null ? r.getEstado().name() : "-",
                        safeDate(r.getFechaEmision()),
                        safeDate(r.getFechaCaducidad()),
                        r.getItems() != null ? String.valueOf(r.getItems().size()) : "0"
                }).toList());
    }

    public byte[] reporteExpedientesPdf() {
        List<ExpedienteMedico> expedientes = expedienteRepository.findAll();

        return crearDocumento("Reporte de Expedientes", new String[] { "Expediente", "Paciente", "Médico", "Fecha Entrada",
                "Diagnóstico", "Tratamiento", "Tarifa Médico", "Monto Consulta" },
                expedientes.stream().flatMap(exp -> exp.getEntradas().stream().map(en -> rowExpediente(exp, en))).toList());
    }

    private String[] rowExpediente(ExpedienteMedico expediente, EntradaHistorial entrada) {
        Medico medico = entrada.getMedico();
        Cita cita = entrada.getCita();
        return new String[] {
                String.valueOf(expediente.getId()),
                expediente.getPaciente() != null ? expediente.getPaciente().getNombre() : "-",
                nombreCompleto(medico),
                safeDate(entrada.getFecha()),
                safe(entrada.getDiagnostico()),
                safe(entrada.getTratamiento()),
                money(medico != null ? medico.getTarifaConsulta() : null),
                money(cita != null ? cita.getTarifaAplicada() : null)
        };
    }

    public byte[] reportePacientesPdf() {
        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .sorted(Comparator.comparing(Paciente::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return crearDocumento("Reporte de Pacientes", new String[] { "Nombre", "Identificación", "Nacimiento", "Alergias",
                "Enfermedades" }, pacientes.stream().map(p -> new String[] {
                        safe(p.getNombre()),
                        safe(p.getNumeroIdentificacion()),
                        safeDate(p.getFechaNacimiento()),
                        p.getAlergias() != null ? String.valueOf(p.getAlergias().size()) : "0",
                        p.getEnfermedades() != null ? String.valueOf(p.getEnfermedades().size()) : "0"
                }).toList());
    }

    public byte[] reporteMedicosPdf() {
        List<Medico> medicos = medicoRepository.findAll().stream()
                .sorted(Comparator.comparing(Medico::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return crearDocumento("Reporte de Médicos", new String[] { "Nombre", "Licencia", "Especialidad", "Estado", "Disponible",
                "Tarifa Consulta" }, medicos.stream().map(m -> new String[] {
                        nombreCompleto(m),
                        safe(m.getNumeroLicencia()),
                        m.getEspecialidad() != null ? m.getEspecialidad().name() : "-",
                        m.getEstado() != null ? m.getEstado().name() : "-",
                        m.isDisponible() ? "Sí" : "No",
                        money(m.getTarifaConsulta())
                }).toList());
    }

    public byte[] reporteMedicamentosPdf() {
        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .sorted(Comparator.comparing(Medicamento::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return crearDocumento("Reporte de Medicamentos", new String[] { "ID", "Nombre", "Descripción" },
                medicamentos.stream().map(m -> new String[] {
                        String.valueOf(m.getId()),
                        safe(m.getNombre()),
                        safe(m.getDescripcion())
                }).toList());
    }

    private byte[] crearDocumento(String titulo, String[] headers, List<String[]> rows) {
        Document doc = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
        doc.add(new Paragraph(titulo, titleFont));
        doc.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (String[] row : rows) {
            for (String value : row) {
                table.addCell(new Phrase(safe(value)));
            }
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String safeDate(java.time.LocalDate date) {
        return date == null ? "-" : date.format(DF);
    }

    private String money(BigDecimal value) {
        return value == null ? "-" : "$" + value;
    }

    private String nombreCompleto(Medico medico) {
        if (medico == null) {
            return "-";
        }
        return safe(medico.getNombre()) + " " + safe(medico.getApellido());
    }
}
