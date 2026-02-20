import { Component } from '@angular/core';
import { ObtenerExpedienteMedicoDTO } from '../../../DTO/paciente-ver-expedi';
import { PacienteService } from '../../../services/paciente.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-paciente-expediente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './paciente-expediente.component.html',
  styleUrl: './paciente-expediente.component.css'
})
export class PacienteExpedienteComponent {

  paciente: ObtenerExpedienteMedicoDTO | null = null;
    error: string = '';
  
    constructor(private pacienteService: PacienteService) { }
  
    ngOnInit(): void {
      this.pacienteService.getExpediente().subscribe({
        next: data => this.paciente = data,
        error: err => {
          console.error(err);
          this.error = 'No tienes permiso o hubo un error al cargar el expediente';
        }
      });
    }

}
