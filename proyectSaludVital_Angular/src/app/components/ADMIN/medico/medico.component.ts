import { Component, OnInit } from '@angular/core';
import { MedicoService } from '../../../services/medico.service';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ObtenerMedicoDTO } from '../../../DTO/obtener-medico-dto';
import { MedicoConMostrar } from '../../../DTO/medico-horario';
import { UsuarioAdminService } from '../../../services/usuario-admin.service';
import { UsuarioBusquedaDTO } from '../../../DTO/usuario-busqueda-dto';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './medico.component.html',
  styleUrl: './medico.component.css'
})
export class MedicoComponent implements OnInit {
  medicos: MedicoConMostrar[] = [];
  form: FormGroup;
  editar = false;
  medicoSeleccionadoId: number | null = null;
  mensajeError: string | null = null;
  mensajeExito: string | null = null;
  emailBusquedaUsuario = '';
  sugerenciasUsuarios: UsuarioBusquedaDTO[] = [];
  selectedImageFile: File | null = null;
  readonly apiUrl = environment.apiUrl;

  paginaActual = 1;
  readonly registrosPorPagina = 10;

  especialidades = ['CARDIOLOGIA', 'PEDIATRIA', 'DERMATOLOGIA', 'GINECOLOGIA', 'NEUROLOGIA'];
  diasSemana = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

  constructor(
    private medicoService: MedicoService,
    private usuarioAdminService: UsuarioAdminService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      numeroLicencia: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(10)]],
      telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
      email: ['', [Validators.required, Validators.email]],
      especialidad: ['', Validators.required],
      tarifaConsulta: [0, Validators.required],
      usuarioId: [null, Validators.required],
      imagenUrl: [''],
      disponible: [null, Validators.required],
      estado: ['ACTIVO', Validators.required],
      horarios: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.listarMedicos();
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.medicos.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get medicosPaginados(): MedicoConMostrar[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.medicos.slice(inicio, inicio + this.registrosPorPagina);
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

  get horarios(): FormArray {
    return this.form.get('horarios') as FormArray;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedImageFile = input.files?.[0] || null;
  }

  resolveImageUrl(path?: string): string {
    if (!path) return '/images/no-image-person.svg';
    if (path.startsWith('http')) return path;
    if (path.startsWith('/files/')) return `${this.apiUrl}${path}`;
    if (path.startsWith('files/')) return `${this.apiUrl}/${path}`;
    return `${this.apiUrl}/files/${path.replace(/^\/+/, '')}`;
  }

  listarMedicos() {
    this.medicoService.listar().subscribe((data) => {
      this.medicos = data;
      this.paginaActual = 1;
    });
  }

  onEmailInputChange() {
    this.mensajeError = null;
    this.mensajeExito = null;
    const query = this.emailBusquedaUsuario.trim();

    if (query.length < 2) {
      this.sugerenciasUsuarios = [];
      return;
    }

    this.usuarioAdminService.autocompletePorEmail(query).subscribe({
      next: (usuarios) => {
        this.sugerenciasUsuarios = usuarios;
      },
      error: (err) => {
        console.error(err);
        this.sugerenciasUsuarios = [];
      }
    });
  }

  seleccionarUsuario(usuario: UsuarioBusquedaDTO) {
    this.emailBusquedaUsuario = usuario.email;
    this.form.patchValue({ usuarioId: usuario.id });
    this.sugerenciasUsuarios = [];
  }

  agregarHorario() {
    this.horarios.push(
      this.fb.group({
        dia: ['', Validators.required],
        horaInicio: ['', Validators.required],
        horaFin: ['', Validators.required]
      })
    );
  }

  eliminarHorario(index: number) {
    this.horarios.removeAt(index);
  }

  editarMedico(medico: ObtenerMedicoDTO) {
    this.editar = true;
    this.medicoSeleccionadoId = medico.id || null;

    this.form.patchValue({
      nombre: medico.nombre,
      apellido: medico.apellido,
      numeroLicencia: medico.numeroLicencia,
      telefono: medico.telefono,
      email: medico.email,
      especialidad: medico.especialidad,
      tarifaConsulta: medico.tarifaConsulta,
      usuarioId: medico.usuarioId,
      imagenUrl: (medico as any).imagenUrl || '',
      estado: medico.estado,
      disponible: medico.disponible
    });

    this.horarios.clear();
    medico.horarios.forEach((h) => {
      this.horarios.push(
        this.fb.group({
          dia: h.dia,
          horaInicio: h.horaInicio,
          horaFin: h.horaFin
        })
      );
    });
  }

  cancelarEdicion() {
    this.editar = false;
    this.medicoSeleccionadoId = null;
    this.emailBusquedaUsuario = '';
    this.sugerenciasUsuarios = [];
    this.form.reset({ estado: 'ACTIVO', tarifaConsulta: 0, disponible: null });
    this.horarios.clear();
    this.mensajeError = null;
    this.mensajeExito = null;
    this.selectedImageFile = null;
  }

  guardar() {
    if (this.form.invalid) return;

    this.mensajeError = null;
    this.mensajeExito = null;
    const medicoData = this.form.value;
    const requestBody = this.selectedImageFile
      ? (() => { const fd = new FormData(); fd.append('data', new Blob([JSON.stringify(medicoData)], { type: 'application/json' })); fd.append('imagen', this.selectedImageFile as File); return fd; })()
      : medicoData;

    if (this.editar && this.medicoSeleccionadoId) {
      this.medicoService.actualizar(this.medicoSeleccionadoId, requestBody).subscribe({
        next: () => {
          this.listarMedicos();
          this.cancelarEdicion();
          this.mensajeExito = 'Médico actualizado correctamente';
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al actualizar médico';
        }
      });
    } else {
      this.medicoService.agregar(requestBody).subscribe({
        next: () => {
          this.listarMedicos();
          this.cancelarEdicion();
          this.mensajeExito = 'Médico agregado correctamente';
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al agregar médico';
        }
      });
    }
  }
}
