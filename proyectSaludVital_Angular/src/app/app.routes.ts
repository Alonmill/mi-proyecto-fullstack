import { Routes } from '@angular/router';
import { MedicamentosComponent } from './components/ADMIN/medicamentos/medicamentos.component';
import { MedicoComponent } from './components/ADMIN/medico/medico.component';
import { AuthGuard } from './guards/AuthGuard';
import { PacienteComponent } from './components/ADMIN/paciente/paciente.component';
import { PerfilPacienteComponent } from './components/PACIENTE/perfil-paciente/perfil-paciente.component';
import { PacienteExpedienteComponent } from './components/PACIENTE/paciente-expediente/paciente-expediente.component';
import { NoAutorizadoComponent } from './components/PUBLICO/no-autorizado/no-autorizado.component';
import { ListadoMedicamentosComponent } from './components/PUBLICO/listado-medicamentos/listado-medicamentos.component';
import { PacienteLayoutComponent } from './components/PACIENTE/paciente-layout/paciente-layout.component';
import { AdminLayoutComponent } from './components/ADMIN/admin-layout/admin-layout.component';
import { MedicoLayoutComponent } from './components/MEDICO/medico-layout/medico-layout.component';
import { PacienteMisRecetasComponent } from './components/PACIENTE/paciente-mis-recetas/paciente-mis-recetas.component';
import { RecetaAgregarComponent } from './components/MEDICO/receta-agregar/receta-agregar.component';
import { RecetaActualizarComponent } from './components/MEDICO/receta-actualizar/receta.component';
import { ListadoRecetaComponent } from './components/ADMIN/listado-receta/listado-receta.component';
import { ActualizarCitaComponent } from './components/PACIENTE/actualizar-cita/actualizar-cita.component';

import { ActualizarCitaAdminComponent } from './components/ADMIN/actualizar-cita-admin/actualizar-cita-admin.component';
import { VerExpedienteMedicoComponent } from './components/MEDICO/expedientes-medico/expedientes-medico.component';

import { ListadoExpedientesComponent } from './components/ADMIN/listado-expediente/listado-expediente.component';
import { RegistroComponent } from './components/PUBLICO/registro/registro.component';
import { LoginComponent } from './components/PUBLICO/login/login.component';
import { CrearCitaComponent } from './components/PACIENTE/crear-cita/crear-cita.component';
import { CancelarCitaPacienteComponent } from './components/PACIENTE/cancelar-cita-paciente/cancelar-cita-paciente.component';
import { MisCitasPacienteComponent } from './components/PACIENTE/mis-citas-paciente/mis-citas-paciente.component';
import { PerfilMedicoComponent } from './components/MEDICO/perfil-medico/perfil-medico.component';
import { ActualizarPerfilMedicoComponent } from './components/MEDICO/actualizar-perfil-medico/actualizar-perfil-medico.component';
import { CrearExpedienteComponent } from './components/MEDICO/crear-expediente/crear-expediente.component';
import { CancelarCitaComponent } from './components/ADMIN/cancelar-cita-admin/cancelar-cita.component';
import { ListadoCitasComponent } from './components/ADMIN/listado-citas/listado-citas.component';
import { ReportesComponent } from './components/ADMIN/reportes/reportes.component';

export const routes: Routes = [

  {
    path: 'registro',
    component: RegistroComponent
  },
  {
    path : 'login',
    component : LoginComponent,
    
  },

  {
  path: 'paciente',
  component: PacienteLayoutComponent,
  canActivate: [AuthGuard],
  data: { roles: ['PACIENTE'] },
  children: [
    { path: '',redirectTo: "perfil-paciente" , pathMatch: "full" },
    { path: 'perfil-paciente', component: PerfilPacienteComponent },
    { path: 'expediente-paciente', component: PacienteExpedienteComponent },
    { path: 'mis-recetas', component: PacienteMisRecetasComponent },

    // Citas solo PACIENTE
    { path: 'crear-cita', component: CrearCitaComponent },          // PACIENTE
    { path: 'actualizar-cita', component: ActualizarCitaComponent }, // PACIENTE
    { path: 'cancelar-cita-paciente', component: CancelarCitaPacienteComponent },     // PACIENTE
    { path: 'mis-citas', component: MisCitasPacienteComponent }      // PACIENTE
  ]
},
  // Rutas MEDICO
{
  path: 'medico',
  component: MedicoLayoutComponent,
  canActivate: [AuthGuard],
  data: { roles: ['MEDICO'] },
  children: [
    { path: '',redirectTo: "perfil-medico" , pathMatch: "full" },
    { path: 'perfil-medico', component: PerfilMedicoComponent },
    { path: 'perfil-medico/actualizar', component: ActualizarPerfilMedicoComponent },
    { path: 'receta-agregar', component: RecetaAgregarComponent },
    { path: 'receta-agregar/:idPaciente', component: RecetaAgregarComponent },
    { path: 'receta-actualizar', component: RecetaActualizarComponent },
    { path: 'lista-recetas', component: RecetaActualizarComponent },

    // Expedientes solo MEDICO
    { path: 'crear-expediente', component: CrearExpedienteComponent },
    { path: 'ver-expediente/:id', component: VerExpedienteMedicoComponent }
  ]
},

// Rutas ADMIN
{
  path: 'admin',
  component: AdminLayoutComponent,
  canActivate: [AuthGuard],
  data: { roles: ['ADMIN'] },
  children: [
    { path: 'medicamento', component: MedicamentosComponent },
    { path: 'medico', component: MedicoComponent },
    { path: 'paciente', component: PacienteComponent },
    { path: 'listado-receta', component: ListadoRecetaComponent },

    // Citas solo ADMIN
    { path: 'actualizar-cita', component: ActualizarCitaAdminComponent },
    { path: 'cancelar-cita', component: CancelarCitaComponent },
    { path: 'listado-citas', component: ListadoCitasComponent },
    { path: 'reportes', component: ReportesComponent },

    // Expedientes solo ADMIN
    { path: 'listado-expedientes', component: ListadoExpedientesComponent }
  ]
},
  
  {
  path: 'no-autorizado',
  component: NoAutorizadoComponent
},
   { path: '', redirectTo: '/login', pathMatch: 'full' }
];
