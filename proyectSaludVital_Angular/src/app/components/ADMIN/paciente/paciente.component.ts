import { Component } from '@angular/core';
import { PacienteConMostrar } from '../../../DTO/paciente-enfermedad-alergia';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PacienteService } from '../../../services/paciente.service';
import { ObtenerPacienteDTO } from '../../../DTO/obtener-paciente-dto';
import { UsuarioAdminService } from '../../../services/usuario-admin.service';
import { UsuarioBusquedaDTO } from '../../../DTO/usuario-busqueda-dto';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './paciente.component.html',
  styleUrl: './paciente.component.css'
})
export class PacienteComponent {
  pacientes: PacienteConMostrar[] = [];
  form: FormGroup;
  editar = false;
  pacienteSeleccionadoId: number | null = null;
  mensajeError: string | null = null;
  mensajeExito: string | null = null;
  emailBusquedaUsuario = '';
  sugerenciasUsuarios: UsuarioBusquedaDTO[] = [];
  selectedImageFile: File | null = null;
  readonly apiUrl = environment.apiUrl;

  paginaActual = 1;
  readonly registrosPorPagina = 10;

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.pacientes.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get pacientesPaginados(): PacienteConMostrar[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.pacientes.slice(inicio, inicio + this.registrosPorPagina);
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
    private pacienteService: PacienteService,
    private usuarioAdminService: UsuarioAdminService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      numeroIdentificacion: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      fechaNacimiento: ['', Validators.required],
      alergias: this.fb.array([]),
      enfermedades: this.fb.array([]),
      usuarioId: [null, [Validators.required, Validators.pattern(/^\d+$/)]],
      imagenUrl: ['']
    });
  }

  ngOnInit(): void {
    this.listarPacientes();
  }

  get alergias(): FormArray {
    return this.form.get('alergias') as FormArray;
  }

  get enfermedades(): FormArray {
    return this.form.get('enfermedades') as FormArray;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedImageFile = input.files?.[0] || null;
  }

  resolveImageUrl(path?: string): string {
    if (!path) return '/images/no-image-person.svg';
    if (path.startsWith('http')) return path;
    return `${this.apiUrl}/files/${path}`;
  }

  listarPacientes() {
    this.pacienteService.listar().subscribe((data) => {
      this.pacientes = data;
      this.paginaActual = 1;
    });
  }

  onEmailInputChange() {
    const query = this.emailBusquedaUsuario.trim();
    if (query.length < 2) {
      this.sugerenciasUsuarios = [];
      return;
    }

    this.usuarioAdminService.autocompletePorEmail(query).subscribe({
      next: (usuarios) => {
        this.sugerenciasUsuarios = usuarios;
      },
      error: () => {
        this.sugerenciasUsuarios = [];
      }
    });
  }

  seleccionarUsuario(usuario: UsuarioBusquedaDTO) {
    this.emailBusquedaUsuario = usuario.email;
    this.form.patchValue({ usuarioId: usuario.id });
    this.sugerenciasUsuarios = [];
  }

  agregarAlergia() {
    this.alergias.push(this.fb.group({ nombre: ['', Validators.required] }));
  }

  agregarEnfermedad() {
    this.enfermedades.push(this.fb.group({ nombre: ['', Validators.required] }));
  }

  eliminarAlergia(index: number) {
    this.alergias.removeAt(index);
  }

  eliminarEnfermedad(index: number) {
    this.enfermedades.removeAt(index);
  }

  editarPaciente(paciente: ObtenerPacienteDTO) {
    this.editar = true;
    this.pacienteSeleccionadoId = paciente.id || null;

    this.form.patchValue({
      nombre: paciente.nombre,
      numeroIdentificacion: paciente.numeroIdentificacion,
      fechaNacimiento: paciente.fechaNacimiento,
      usuarioId: paciente.usuarioId,
      imagenUrl: (paciente as any).imagenUrl || ''
    });

    this.alergias.clear();
    paciente.alergias.forEach((a) => {
      this.alergias.push(this.fb.group({ nombre: [a, Validators.required] }));
    });

    this.enfermedades.clear();
    paciente.enfermedades.forEach((e) => {
      this.enfermedades.push(this.fb.group({ nombre: [e, Validators.required] }));
    });
  }

  cancelarEdicion() {
    this.editar = false;
    this.pacienteSeleccionadoId = null;
    this.emailBusquedaUsuario = '';
    this.sugerenciasUsuarios = [];
    this.form.reset();
    this.alergias.clear();
    this.enfermedades.clear();
    this.mensajeError = null;
    this.mensajeExito = null;
    this.selectedImageFile = null;
  }

  guardar() {
    if (this.form.invalid) return;

    this.mensajeError = null;
    this.mensajeExito = null;
    this.selectedImageFile = null;

    const pacienteData = {
      ...this.form.value,
      alergias: this.form.value.alergias.map((a: any) => a.nombre),
      enfermedades: this.form.value.enfermedades.map((e: any) => e.nombre)
    };

    const requestBody = this.selectedImageFile
      ? (() => { const fd = new FormData(); fd.append('data', new Blob([JSON.stringify(pacienteData)], { type: 'application/json' })); fd.append('imagen', this.selectedImageFile as File); return fd; })()
      : pacienteData;

    if (this.editar && this.pacienteSeleccionadoId) {
      this.pacienteService.actualizar(this.pacienteSeleccionadoId, requestBody).subscribe({
        next: () => {
          this.listarPacientes();
          this.cancelarEdicion();
          this.mensajeExito = "Paciente actualizado correctamente";
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al actualizar paciente';
        }
      });
    } else {
      this.pacienteService.agregar(requestBody).subscribe({
        next: () => {
          this.listarPacientes();
          this.cancelarEdicion();
          this.mensajeExito = "Paciente actualizado correctamente";
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al agregar paciente';
        }
      });
    }
  }
}
