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

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.get<ExpedienteMedicoDTO[]>('expedientes/listado')
      .subscribe({
        next: data => this.expedientes = data,
        error: err => {
          console.error(err);
          this.error = 'Error al cargar expedientes';
        }
      });
  }
}
