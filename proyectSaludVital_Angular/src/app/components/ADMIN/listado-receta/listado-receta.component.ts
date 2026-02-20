import { Component } from '@angular/core';
import { RecetaService } from '../../../services/receta.service';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-listado-receta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './listado-receta.component.html',
  styleUrl: './listado-receta.component.css'
})
export class ListadoRecetaComponent {
  recetas: any[] = [];
    cargando: boolean = true;
  
    constructor(private recetaService: RecetaService) {}
  
    ngOnInit(): void {
      this.listarRecetas();
    }
  
    listarRecetas(): void {
      this.cargando = true;
      this.recetaService.listar().subscribe({
        next: (data) => {
          this.recetas = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error al listar recetas', err);
          this.cargando = false;
        }
      });
    }
}
