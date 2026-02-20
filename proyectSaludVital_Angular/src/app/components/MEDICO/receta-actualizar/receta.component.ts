import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RecetaConMostrar } from '../../../DTO/receta-items';
import { RecetaService } from '../../../services/receta.service';
import { ObtenerRecetaDTO } from '../../../DTO/obtener-receta-dto';

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
    editar: boolean = false;
    recetaSeleccionadoId: number | null = null;
    mensajeExito: string = '';
mensajeError: string = '';

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
  this.recetaService.listar().subscribe(data => {
    console.log('Recetas desde Angular:', data);
    this.recetas = data;
  });
}
      
        agregarItem() {
          this.items.push(this.fb.group({
            medicamentoId: [null, Validators.required],
            dosis: ['', Validators.required],
            frecuencia: ['', Validators.required]
          }));
        }
      
        eliminarItem(index: number) {
          this.items.removeAt(index);
        }

        onSubmit() {
  if (this.form.valid) {
    const recetaParaBackend = {
      idReceta: this.form.value.idReceta,
      medicoNombre: this.form.value.medicoNombre,
      pacienteNombre: this.form.value.pacienteNombre,
      fechaEmision: this.form.value.fechaEmision,
      fechaCaducidad: this.form.value.fechaCaducidad,
      items: this.items.controls.map(ctrl => ({
        medicamentoId: ctrl.get('medicamentoId')?.value,
        dosis: ctrl.get('dosis')?.value,
        frecuencia: ctrl.get('frecuencia')?.value
      }))
    };

    this.recetaService.actualizar(recetaParaBackend).subscribe({
      next: (recetaActualizada) => {
        // 1️⃣ Actualizar la lista local
        const index = this.recetas.findIndex(r => r.id === recetaActualizada.id);
        if (index !== -1) {
          this.recetas[index] = recetaActualizada;
        }

        // 2️⃣ Limpiar el formulario
        this.form.reset();
        this.items.clear();
        this.editar = false;
        this.recetaSeleccionadoId = null;

        // 3️⃣ Mostrar mensaje de éxito
        this.mensajeExito = `✅ Receta actualizada correctamente`;
        setTimeout(() => this.mensajeExito = '', 3000); // desaparece después de 3s
      },
      error: (err) => {
        console.error('❌ Error al actualizar', err);
        this.mensajeError = `❌ Error al actualizar`;
        setTimeout(() => this.mensajeError = '', 3000);
      }
    });
  }
}


      editarReceta(receta: ObtenerRecetaDTO) {
  console.log('Receta a editar:', receta);
  this.editar = true;
  this.recetaSeleccionadoId = receta.id;

  this.form.patchValue({
  idReceta: receta.id,  // <- muy importante
  medicoNombre: receta.medicoNombre,
  pacienteNombre: receta.pacienteNombre,
  fechaEmision: receta.fechaEmision.split('T')[0],
  fechaCaducidad: receta.fechaCaducidad.split('T')[0]
});

  this.items.clear();

  receta.items.forEach(i => {
    this.items.push(this.fb.group({
      medicamentoId: [i.medicamentoId, Validators.required],
      dosis: [i.dosis, Validators.required],
      frecuencia: [i.frecuencia, Validators.required]
    }));
  });
}


}
