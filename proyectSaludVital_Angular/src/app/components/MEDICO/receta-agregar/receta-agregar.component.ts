import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RecetaService } from '../../../services/receta.service';
import { AgregarRecetaDTO } from '../../../DTO/agregar-receta-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MedicamentosService } from '../../../services/medicamentos.service';
import { NgSelectModule } from '@ng-select/ng-select';

interface MedicamentoOption {
  id: number;
  nombre: string;
}

@Component({
  selector: 'app-receta-agregar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgSelectModule],
  templateUrl: './receta-agregar.component.html',
  styleUrl: './receta-agregar.component.css'
})
export class RecetaAgregarComponent implements OnInit {
  form: FormGroup;
  idPaciente!: number;
  mensajeError: string | null = null;
  medicamentos: MedicamentoOption[] = [];

  constructor(
    private recetaService: RecetaService,
    private medicamentoService: MedicamentosService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.idPaciente = +params['idPaciente'];
      if (!this.idPaciente) {
        this.mensajeError = 'Debe iniciar la receta desde una atención con expediente.';
      }
    });

    this.cargarMedicamentos();
    this.agregarItem();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  cargarMedicamentos() {
    this.medicamentoService.listar().subscribe({
      next: (meds) => {
        this.medicamentos = meds.map((med) => ({
          id: med.id,
          nombre: med.nombre
        }));
      },
      error: (err) => {
        console.error('Error al cargar medicamentos', err);
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
    this.mensajeError = null;

    if (!this.idPaciente) {
      this.mensajeError = 'Debe indicar un paciente válido para emitir la receta.';
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
        error: (err) => {
          console.error('Error al crear receta', err);

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
