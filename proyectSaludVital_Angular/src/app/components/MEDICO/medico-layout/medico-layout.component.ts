import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MedicoService } from '../../../services/medico.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-medico-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './medico-layout.component.html',
  styleUrl: './medico-layout.component.css'
})
export class MedicoLayoutComponent implements OnInit {
  nombreMedico = 'Dr. Médico';
  inicialesMedico = 'DM';
  imagenMedicoUrl = '';
  readonly apiUrl = environment.apiUrl;

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = '/images/no-image-person.svg';
  }

  constructor(private medicoService: MedicoService) {}

  ngOnInit(): void {
    this.medicoService.getPerfil().subscribe({
      next: (medico) => {
        const nombre = (medico?.nombre || '').trim();
        const apellido = (medico?.apellido || '').trim();

        if (!nombre && !apellido) {
          return;
        }

        this.nombreMedico = [nombre, apellido].filter(Boolean).join(' ');
        const base = `${nombre.charAt(0)}${apellido.charAt(0)}`.trim();
        this.inicialesMedico = (base || nombre.slice(0, 2) || 'DM').toUpperCase();
        const ruta = (medico as any).imagenUrl || '';
        this.imagenMedicoUrl = ruta ? `${this.apiUrl}/files/${ruta}` : '';
      }
    });
  }
}
