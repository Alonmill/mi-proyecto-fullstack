import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ExpedienteMedicoDTO } from '../../../DTO/Expediente-medico-dto';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-expedientes-medico',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,FormsModule],
  templateUrl: './expedientes-medico.component.html',
  styleUrl: './expedientes-medico.component.css'
})
export class VerExpedienteMedicoComponent {
  expediente: ExpedienteMedicoDTO | null = null;
  idBuscar: number | null = null;
  error: string = '';

  constructor(private api: ApiService) {}

  buscar() {
    if (!this.idBuscar) return;

    this.api.get<ExpedienteMedicoDTO>(`expedientes/ver/${this.idBuscar}`)
      .subscribe({
        next: data => this.expediente = data,
        error: err => {
          console.error(err);
          this.error = 'No se pudo cargar el expediente';
        }
      });
  }
}