import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server'; // 👈 este es el correcto
import { appConfig } from './app.config';


const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),   // 👈 ya no necesitas withRoutes
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);