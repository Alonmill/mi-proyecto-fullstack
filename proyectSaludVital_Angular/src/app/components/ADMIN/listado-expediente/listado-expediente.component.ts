import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';
import { ExpedienteMedicoDTO } from '../../../DTO/Expediente-medico-dto';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-listado-expediente',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './listado-expediente.component.html',
  styleUrl: './listado-expediente.component.css'
})
export class ListadoExpedientesComponent implements OnInit {
  expedientes: ExpedienteMedicoDTO[] = [];
  error: string = '';
  paginaActual = 1;
  readonly registrosPorPagina = 10;

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.expedientes.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get expedientesPaginados(): ExpedienteMedicoDTO[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.expedientes.slice(inicio, inicio + this.registrosPorPagina);
  }

  irPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas) return;
    this.paginaActual = pagina;
  }

  paginaAnterior(): void {
    this.irPagina(this.paginaActual - 1);
  }

  paginaSiguiente(): void {
    this.irPagina(this.paginaActual + 1);
  }

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.get<ExpedienteMedicoDTO[]>('expedientes/listado')
      .subscribe({
        next: data => {
          this.expedientes = data;
          this.paginaActual = 1;
        },
        error: err => {
          console.error(err);
          this.error = 'Error al cargar expedientes';
        }
      });
  }
}
