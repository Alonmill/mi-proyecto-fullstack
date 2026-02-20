import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ObtenerRecetaDTO } from '../../../DTO/obtener-receta-dto';
import { RecetaService } from '../../../services/receta.service';

@Component({
  selector: 'app-paciente-mis-recetas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './paciente-mis-recetas.component.html',
  styleUrl: './paciente-mis-recetas.component.css'
})
export class PacienteMisRecetasComponent {
  recetas: ObtenerRecetaDTO[] = [];
  error: string = '';

  constructor(private medicoService: RecetaService) { }

ngOnInit(): void {
  this.medicoService.getRecetas().subscribe({
    next: data => this.recetas = data,  // ahora data es un array
    error: err => {
      console.error(err);
      this.error = 'No tienes permiso o hubo un error al cargar las recetas';
    }
  });
}
}

