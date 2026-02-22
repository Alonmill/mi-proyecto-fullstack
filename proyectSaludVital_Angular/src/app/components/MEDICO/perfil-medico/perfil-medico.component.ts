import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MedicoService } from '../../../services/medico.service';
import { ObtenerMedicoDTO } from '../../../DTO/obtener-medico-dto';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-perfil-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil-medico.component.html',
  styleUrl: './perfil-medico.component.css'
})
export class PerfilMedicoComponent implements OnInit {

  medico: ObtenerMedicoDTO | null = null;
  error: string = '';
  readonly apiUrl = environment.apiUrl;

  constructor(
    private medicoService: MedicoService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.medicoService.getPerfil().subscribe({
      next: data => this.medico = data,
      error: err => {
        console.error(err);
        this.error = 'No tienes permiso o hubo un error al cargar el perfil';
      }
    });
  }

  resolveImageUrl(path?: string): string {
    if (!path) return '/images/no-image-person.svg';
    if (path.startsWith('http')) return path;
    return `${this.apiUrl}/files/${path}`;
  }

  irActualizarPerfil(): void {
    this.router.navigate(['/medico/perfil-medico/actualizar']);
  }

}
