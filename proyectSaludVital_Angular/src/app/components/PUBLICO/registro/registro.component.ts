import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup,Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { Router } from '@angular/router';
import { RegistroService } from '../../../services/registro.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent implements OnInit {
  registroForm!: FormGroup;
  submitted = false;

  rolesDisponibles: string[] = ['ADMIN', 'PACIENTE', 'MEDICO']; // 👈 los 3 roles

  constructor(
    private fb: FormBuilder,
    private registroService: RegistroService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registroForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(2)]],
      rol: ['PACIENTE', Validators.required] // 👈 rol por defecto
    });
  }

  get f() { return this.registroForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.registroForm.invalid) {
      return;
    }

    // Armar objeto en el formato de tu entidad Usuario
    const userData = {
      name: this.registroForm.value.name,
      email: this.registroForm.value.email,
      password: this.registroForm.value.password,
      role: this.registroForm.value.rol // 👈 backend espera Set<Role>
    };

    this.registroService.registrarUsuario(userData).subscribe({
      next: (resp: any) => {
        console.log('Usuario registrado:', resp);
        alert('Usuario registrado correctamente');
        this.registroForm.reset();
        this.submitted = false;
      },
      error: (err:any) => {
        console.error('Error al registrar usuario:', err);
        alert('Error al registrar usuario');
      }
    });
  }
}