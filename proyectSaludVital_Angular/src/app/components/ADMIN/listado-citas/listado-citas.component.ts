import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';

@Component({
  selector: 'app-listado-citas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './listado-citas.component.html',
  styleUrl: './listado-citas.component.css'
})
export class ListadoCitasComponent implements OnInit {
  citas: any[] = [];

  constructor(private citaService: CitaService) {}

  ngOnInit(): void {
    this.citaService.listar().subscribe({
      next: data => this.citas = data,
      error: err => console.error('Error al obtener listado de citas', err)
    });
  }
}