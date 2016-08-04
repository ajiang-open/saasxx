import { bootstrap }    from '@angular/platform-browser-dynamic';

import { AppComponent } from './app.component';
import { appRouterProviders } from './app.routes';
import {enableProdMode} from '@angular/core';
import {provideForms, disableDeprecatedForms} from '@angular/forms';

enableProdMode();

bootstrap(AppComponent, [
    appRouterProviders,disableDeprecatedForms(), provideForms()
]);