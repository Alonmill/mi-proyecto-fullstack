import { Component, OnInit } from '@angular/core';
import { MedicamentosService } from '../../../services/medicamentos.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-listado-medicamentos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './listado-medicamentos.component.html',
  styleUrl: './listado-medicamentos.component.css'
})
export class ListadoMedicamentosComponent implements OnInit {
  medicamentos: any[] = [];
  cargando: boolean = true;

  constructor(private medicamentoService: MedicamentosService) {}

  ngOnInit(): void {
    this.listarMedicamentos();
  }

  listarMedicamentos(): void {
    this.cargando = true;
    this.medicamentoService.listar().subscribe({
      next: (data) => {
        this.medicamentos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al listar medicamentos', err);
        this.cargando = false;
      }
    });
  }
}

