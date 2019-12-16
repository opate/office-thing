import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { WorkPeriodComponent } from './work-period/work-period.component';
import { UserComponent } from './user/user.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientService } from './service/http-client.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule, MatCardModule, MatDialogModule } from '@angular/material';
import { MatToolbarModule, MatIconModule, MatSidenavModule, MatListModule, MatButtonModule } from  '@angular/material';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material';
import { FlexLayoutModule } from '@angular/flex-layout';
import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HomeComponent } from './home/home.component';
import { BasicAuthHttpInterceptorService } from './service/basic-auth-http-interceptor.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LiveComponent } from './live/live.component';
import { DialogBoxComponent } from './dialog-box/dialog-box.component';

@NgModule({
  declarations: [
    AppComponent,
    WorkPeriodComponent,
    UserComponent,
    LoginComponent,
    LogoutComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    LiveComponent,
    DialogBoxComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatTableModule,
    MatCheckboxModule,
    MatToolbarModule,
    MatSidenavModule,
    MatDialogModule,
    MatListModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    FlexLayoutModule,
    FormsModule,
    ReactiveFormsModule
  ],
  entryComponents: [
    DialogBoxComponent
  ],
  providers: [
    {  
      provide:HTTP_INTERCEPTORS, useClass:BasicAuthHttpInterceptorService, multi:true 
    },
    HttpClientService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

