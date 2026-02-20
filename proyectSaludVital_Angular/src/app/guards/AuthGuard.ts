import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from "../services/auth.service";



@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
  if (this.authService.isTokenExpired()) {
    console.warn("⚠️ Token expirado. Redirigiendo a login...");
    this.authService.logout();
    return false;
  }

  const usuario = this.authService.getUsuario();
  const rolesPermitidos = route.data['roles'] as Array<string>;

  if (!usuario) {
    this.router.navigate(['/login']);
    return false;
  }

  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    this.router.navigate(['/no-autorizado']);
    return false;
  }

  return true;
}

}
