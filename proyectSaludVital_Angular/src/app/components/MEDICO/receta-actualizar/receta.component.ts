import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActualizarRecetaDTO } from '../../../DTO/actualizar-receta-dto';
import { EstadoReceta, ObtenerRecetaDTO } from '../../../DTO/obtener-receta-dto';
import { RecetaConMostrar } from '../../../DTO/receta-items';
import { RecetaService } from '../../../services/receta.service';

@Component({
  selector: 'app-receta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './receta.component.html',
  styleUrl: './receta.component.css'
})
export class RecetaActualizarComponent {
  recetas: RecetaConMostrar[] = [];
  form: FormGroup;
  editar = false;
  recetaSeleccionadoId: number | null = null;
  mensajeExito = '';
  mensajeError = '';
  cargando = false;
  recetaDetalle: ObtenerRecetaDTO | null = null;

  paginaActual = 1;
  readonly registrosPorPagina = 10;

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.recetas.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get recetasPaginadas(): RecetaConMostrar[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.recetas.slice(inicio, inicio + this.registrosPorPagina);
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

  constructor(
    private recetaService: RecetaService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      idReceta: [null],
      medicoNombre: [''],
      pacienteNombre: [''],
      fechaEmision: [''],
      fechaCaducidad: [''],
      estado: [''],
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.listarRecetas();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  listarRecetas() {
    this.cargando = true;
    this.recetaService.listar().subscribe({
      next: data => {
        this.recetas = data;
        this.paginaActual = 1;
        this.cargando = false;
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las recetas.';
        this.cargando = false;
      }
    });
  }

  agregarItem() {
    this.items.push(
      this.fb.group({
        medicamentoId: [null, Validators.required],
        dosis: ['', Validators.required],
        frecuencia: ['', Validators.required]
      })
    );
  }

  eliminarItem(index: number) {
    this.items.removeAt(index);
  }

  onSubmit() {
    if (!this.form.valid) {
      this.mensajeError = 'Completa los campos requeridos para guardar.';
      return;
    }

    if (this.form.value.estado !== 'BORRADOR') {
      this.mensajeError = 'Solo se puede editar una receta en estado BORRADOR.';
      return;
    }

    const recetaParaBackend: ActualizarRecetaDTO = {
      idReceta: this.form.value.idReceta,
      items: this.items.controls.map(ctrl => ({
        medicamentoId: ctrl.get('medicamentoId')?.value,
        dosis: ctrl.get('dosis')?.value,
        frecuencia: ctrl.get('frecuencia')?.value
      }))
    };

    this.recetaService.actualizar(recetaParaBackend).subscribe({
      next: recetaActualizada => {
        this.reemplazarRecetaEnLista(recetaActualizada);
        this.resetEdicion();
        this.mensajeExito = '✅ Receta actualizada correctamente.';
        this.mensajeError = '';
      },
      error: err => {
        this.mensajeError = err?.error?.message || '❌ Error al actualizar la receta.';
      }
    });
  }

  editarReceta(receta: ObtenerRecetaDTO) {
    if (receta.estado !== 'BORRADOR') {
      this.mensajeError = 'Solo se puede editar una receta en estado BORRADOR.';
      return;
    }

    this.editar = true;
    this.recetaSeleccionadoId = receta.id;

    this.form.patchValue({
      idReceta: receta.id,
      medicoNombre: receta.medicoNombre,
      pacienteNombre: receta.pacienteNombre,
      fechaEmision: receta.fechaEmision.split('T')[0],
      fechaCaducidad: receta.fechaCaducidad.split('T')[0],
      estado: receta.estado
    });

    this.items.clear();
    receta.items.forEach(i => {
      this.items.push(
        this.fb.group({
          medicamentoId: [i.medicamentoId, Validators.required],
          dosis: [i.dosis, Validators.required],
          frecuencia: [i.frecuencia, Validators.required]
        })
      );
    });

    this.mensajeError = '';
  }

  anularReceta(receta: RecetaConMostrar) {
    this.recetaService.anular(receta.id).subscribe({
      next: data => {
        this.reemplazarRecetaEnLista(data);
        this.mensajeExito = `✅ Receta ${receta.id} anulada.`;
      },
      error: err => (this.mensajeError = err?.error?.message || 'No se pudo anular la receta.')
    });
  }

  duplicarReceta(receta: RecetaConMostrar) {
    this.recetaService.duplicar(receta.id).subscribe({
      next: nueva => {
        this.recetas = [{ ...nueva, showItems: false }, ...this.recetas];
        this.mensajeExito = `✅ Receta ${receta.id} duplicada en nueva receta #${nueva.id}.`;
      },
      error: err => (this.mensajeError = err?.error?.message || 'No se pudo duplicar la receta.')
    });
  }

  renovarReceta(receta: RecetaConMostrar) {
    this.recetaService.renovar(receta.id).subscribe({
      next: nueva => {
        this.recetas = [{ ...nueva, showItems: false }, ...this.recetas];
        this.mensajeExito = `✅ Receta ${receta.id} renovada en nueva receta #${nueva.id}.`;
      },
      error: err => (this.mensajeError = err?.error?.message || 'No se pudo renovar la receta.')
    });
  }

  verReceta(receta: ObtenerRecetaDTO) {
    this.recetaDetalle = receta;
  }

  cerrarDetalle() {
    this.recetaDetalle = null;
  }

  obtenerClaseEstado(estado: EstadoReceta): string {
    const clases: Record<EstadoReceta, string> = {
      BORRADOR: 'badge-borrador',
      EMITIDA: 'badge-emitida',
      DISPENSADA: 'badge-dispensada',
      ANULADA: 'badge-anulada',
      VENCIDA: 'badge-vencida'
    };
    return clases[estado] || 'badge-borrador';
  }

  private reemplazarRecetaEnLista(receta: ObtenerRecetaDTO) {
    const index = this.recetas.findIndex(r => r.id === receta.id);
    if (index !== -1) {
      this.recetas[index] = { ...receta, showItems: this.recetas[index].showItems };
    }
  }

  private resetEdicion() {
    this.form.reset();
    this.items.clear();
    this.editar = false;
    this.recetaSeleccionadoId = null;
  }
}
