import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PacienteService } from '../../../services/paciente.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-paciente-layout',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './paciente-layout.component.html',
  styleUrl: './paciente-layout.component.css'
})
export class PacienteLayoutComponent implements OnInit {
  nombrePaciente = 'Paciente';
  inicialesPaciente = 'PA';
  imagenPacienteUrl = '';
  readonly apiUrl = environment.apiUrl;

  constructor(private pacienteService: PacienteService) {}

  ngOnInit(): void {
    this.pacienteService.getPerfil().subscribe({
      next: (paciente) => {
        const nombre = (paciente?.nombre || '').trim();
        if (!nombre) return;

        this.nombrePaciente = nombre;
        this.inicialesPaciente = this.generarInicialesPaciente(nombre);
        const ruta = (paciente as any).imagenUrl || '';
        this.imagenPacienteUrl = ruta ? `${this.apiUrl}/files/${ruta}` : '';
      }
    });
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = '/images/no-image-person.svg';
  }

  private generarInicialesPaciente(nombreCompleto: string): string {
    const partes = nombreCompleto.split(' ').filter(Boolean);

    if (partes.length >= 2) {
      return `${partes[0][0]}${partes[1][0]}`.toUpperCase();
    }

    return nombreCompleto.slice(0, 2).toUpperCase();
  }

}
