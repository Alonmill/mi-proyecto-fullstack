import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ExpedienteMedicoDTO } from '../../../DTO/Expediente-medico-dto';
import { ExpedienteService } from '../../../services/expediente.service';

interface ExpedienteOption {
  id: number;
  nombrePaciente: string;
}

@Component({
  selector: 'app-expedientes-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './expedientes-medico.component.html',
  styleUrl: './expedientes-medico.component.css'
})
export class VerExpedienteMedicoComponent implements OnInit {
  expediente: ExpedienteMedicoDTO | null = null;
  idBuscar: number | null = null;
  expedientesDisponibles: ExpedienteOption[] = [];
  cargandoExpedientes = false;
  error = '';

  constructor(private expedienteService: ExpedienteService) {}

  ngOnInit(): void {
    this.cargandoExpedientes = true;
    this.expedienteService.getExpedientes().subscribe({
      next: (data) => {
        this.expedientesDisponibles = data.map((expediente) => ({
          id: expediente.id,
          nombrePaciente: expediente.nombrePaciente
        }));
        this.cargandoExpedientes = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'No se pudo cargar la lista de pacientes con expedientes.';
        this.cargandoExpedientes = false;
      }
    });
  }

  buscar() {
    if (!this.idBuscar) {
      return;
    }

    this.error = '';
    this.expediente = null;

    this.expedienteService.getExpedienteById(this.idBuscar).subscribe({
      next: (data) => (this.expediente = data),
      error: (err) => {
        console.error(err);
        this.error = 'No se pudo cargar el expediente';
      }
    });
  }
}
