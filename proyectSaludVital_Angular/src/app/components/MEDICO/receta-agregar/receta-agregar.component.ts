import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RecetaService } from '../../../services/receta.service';
import { AgregarRecetaDTO } from '../../../DTO/agregar-receta-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-receta-agregar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './receta-agregar.component.html',
  styleUrl: './receta-agregar.component.css'
})
export class RecetaAgregarComponent implements OnInit {
  
  form: FormGroup;
  idPaciente!: number;
  mensajeError: string | null = null;

  constructor(
    private recetaService: RecetaService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({ 
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    // obtenemos idPaciente desde la URL
    this.route.params.subscribe(params => {
      this.idPaciente = +params['idPaciente'];
      if (!this.idPaciente) {
        this.mensajeError = 'Debe iniciar la receta desde una atención con expediente.';
      }
    });

    // inicializamos con un item vacío
    this.agregarItem();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
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

  // enviar al backend
  onSubmit() {
  this.mensajeError = null; // resetear errores previos
  if (!this.idPaciente) {
    this.mensajeError = "Debe indicar un paciente válido para emitir la receta.";
    return;
  }

  if (this.form.valid) {
    const receta: AgregarRecetaDTO = this.form.value;
    this.recetaService.agregar(this.idPaciente, receta).subscribe({
      next: () => {
        alert('Receta creada con éxito ✅');
        this.form.reset();
        this.items.clear();
        this.agregarItem();
        this.router.navigate(['/medico/lista-recetas']);
      },
      error: err => {
        console.error('Error al crear receta', err);
        
        // Manejo flexible de errores según lo que mande el backend
        if (err.error?.message) {
          this.mensajeError = err.error.message;
        } else if (typeof err.error === 'string') {
          this.mensajeError = err.error;
        } else {
          this.mensajeError = 'Ocurrió un error al crear la receta. Intenta nuevamente.';
        }
      }
    });
  }
}
}
