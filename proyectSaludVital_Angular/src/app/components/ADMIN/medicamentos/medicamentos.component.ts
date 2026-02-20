import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MedicamentosService } from '../../../services/medicamentos.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-medicamentos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './medicamentos.component.html',
  styleUrls: ['./medicamentos.component.css']
})
export class MedicamentosComponent implements OnInit {
  medicamentoForm!: FormGroup;
  medicamentos: any[] = [];
  isEditMode: boolean = false;
  editarId: number | null = null;

  usuario: any;

  constructor(private fb: FormBuilder, private medicamentoService: MedicamentosService, private authService: AuthService ) {}

  ngOnInit(): void {
// Obtener usuario logueado
    this.usuario = this.authService.getUsuario();

    // Inicializar formulario
    this.medicamentoForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      descripcion: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]]
    });

    // Cargar lista de medicamentos
    this.listarMedicamentos();
  }

  listarMedicamentos(): void {
    this.medicamentoService.listar().subscribe({
      next: (data) => this.medicamentos = data,
      error: (err) => console.error('Error al listar medicamentos', err)
    });
  }

  onSubmit(): void {
    if (this.medicamentoForm.invalid) {
      this.medicamentoForm.markAllAsTouched();
      return;
    }

    if (this.isEditMode && this.editarId) {
      // Editar medicamento
      this.medicamentoService.editar(this.editarId, this.medicamentoForm.value).subscribe({
        next: () => {
          this.listarMedicamentos();
          this.resetForm();
        },
        error: (err) => console.error('Error al editar medicamento', err)
      });
    } else {
      // Registrar medicamento
      this.medicamentoService.registrar(this.medicamentoForm.value).subscribe({
        next: () => {
          this.listarMedicamentos();
          this.resetForm();
        },
        error: (err) => console.error('Error al registrar medicamento', err)
      });
    }
  }

  editarMedicamento(medicamento: any): void {
    this.isEditMode = true;
    this.editarId = medicamento.id;
    this.medicamentoForm.patchValue({
      nombre: medicamento.nombre,
      descripcion: medicamento.descripcion
    });
  }

  resetForm(): void {
    this.medicamentoForm.reset();
    this.isEditMode = false;
    this.editarId = null;
  }
}
