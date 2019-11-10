import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WorkPeriodComponent } from './work-period/work-period.component'
import { UserComponent } from './user/user.component'
import { LoginComponent } from './login/login.component';
import { LiveComponent } from './live/live.component';
import { LogoutComponent } from './logout/logout.component';
import { HomeComponent } from './home/home.component';
import { AuthGuardService } from './service/auth-guard.service';

const routes: Routes = [
  { path: '', component: LoginComponent},
  { path: 'workinghours', component: LoginComponent},
  { path:'workperiod', component: WorkPeriodComponent, canActivate:[AuthGuardService] },
  { path:'user', component: UserComponent, canActivate:[AuthGuardService] },
  { path:'live', component: LiveComponent, canActivate:[AuthGuardService] },
  { path:'login', component: LoginComponent },
  { path:'logout', component: LogoutComponent, canActivate:[AuthGuardService]  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
